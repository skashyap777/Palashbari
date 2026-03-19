import 'package:flutter/material.dart';
import '../../widgets/user_avatar.dart';
import '../../widgets/member_card.dart';
import '../../widgets/filter_bottom_sheet.dart';
import '../../models/member.dart';
import '../../models/paginated_response.dart';
import '../../models/filter_item.dart';
import '../../services/api_service.dart';
import '../../config/dashboard_config.dart';

class HSBTeamScreen extends StatefulWidget {
  const HSBTeamScreen({super.key});

  @override
  State<HSBTeamScreen> createState() => _HSBTeamScreenState();
}

class _HSBTeamScreenState extends State<HSBTeamScreen> {
  final ApiService _apiService = ApiService();
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _searchController = TextEditingController();
  
  final DashboardIcon _dashboardIcon = DashboardConfig.getIconByTitle('HSB Team')!;
  
  List<Member> _members = [];
  bool _isLoading = true;
  bool _isLoadingMore = false;
  int _currentPage = 0;
  final int _pageSize = 10;
  int _totalRecords = 0;
  String? _error;

  Map<String, List<FilterItem>> _selectedFilters = {};

  @override
  void initState() {
    super.initState();
    _loadMembers(refresh: true);
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _scrollController.dispose();
    _searchController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200 &&
        !_isLoadingMore &&
        _members.length < _totalRecords) {
      _loadMembers();
    }
  }

  Future<void> _loadMembers({bool refresh = false}) async {
    if (refresh) {
      setState(() {
        _isLoading = true;
        _currentPage = 0;
        _error = null;
      });
    } else {
      setState(() {
        _isLoadingMore = true;
        _currentPage++;
      });
    }

    try {
      final params = <String, dynamic>{
        ...?_dashboardIcon.defaultParams,
      };

      // Ensure all expected filter keys are present (even if empty)
      for (var category in _dashboardIcon.filterCategories ?? []) {
        params[_getApiKey(category)] = '';
      }

      _selectedFilters.forEach((key, value) {
        if (value.isNotEmpty) {
          params[_getApiKey(key)] = value.map((v) => v.id).join(',');
        }
      });

      final response = await _apiService.fetchPaginatedData(
        endpoint: _dashboardIcon.endpoint,
        start: _currentPage * _pageSize,
        length: _pageSize,
        searchValue: _searchController.text.trim(),
        additionalParams: params,
      );

      final paginatedResponse = PaginatedResponse<Member>.fromJson(
        response,
        (json) => Member.fromJson(json),
      );

      if (mounted) {
        setState(() {
          if (refresh) {
            _members = paginatedResponse.data;
          } else {
            _members.addAll(paginatedResponse.data);
          }
          _totalRecords = paginatedResponse.recordsTotal;
          _isLoading = false;
          _isLoadingMore = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
          _isLoadingMore = false;
        });
      }
    }
  }

  String _getApiKey(String category) {
    if (category == 'booth') return 'polling_booth';
    if (category == 'ward') return 'ward_number';
    return category;
  }

  void _showFilters() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => FilterBottomSheet(
        initialFilters: _selectedFilters.map((key, value) => MapEntry(key, value.map((v) => v.id).toList())),
        categoriesToShow: _dashboardIcon.filterCategories ?? [],
        onApply: (filters) {
          setState(() {
            _selectedFilters = filters;
          });
          _loadMembers(refresh: true);
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Color(0xFF1D1D1F), size: 18),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          _dashboardIcon.title,
          style: const TextStyle(
            color: Color(0xFF1D1D1F),
            fontSize: 17,
            fontWeight: FontWeight.w600,
            letterSpacing: -0.5,
          ),
        ),
        actions: const [
          Padding(
            padding: EdgeInsets.only(right: 16.0),
            child: UserAvatar(radius: 18),
          ),
        ],
      ),
      body: Column(
        children: [
          Container(
            color: Colors.white,
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
            child: Row(
              children: [
                Expanded(
                  child: Container(
                    height: 44,
                    decoration: BoxDecoration(
                      color: const Color(0xFFF2F2F7),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    padding: const EdgeInsets.symmetric(horizontal: 16),
                    child: TextField(
                      controller: _searchController,
                      onChanged: (value) => _loadMembers(refresh: true),
                      decoration: const InputDecoration(
                        hintText: 'Search...',
                        border: InputBorder.none,
                        hintStyle: TextStyle(fontSize: 14),
                      ),
                    ),
                  ),
                ),
                if (_dashboardIcon.filterCategories != null && _dashboardIcon.filterCategories!.isNotEmpty) ...[
                  const SizedBox(width: 12),
                  GestureDetector(
                    onTap: _showFilters,
                    child: Container(
                      width: 44,
                      height: 44,
                      decoration: BoxDecoration(
                        color: primaryColor.withOpacity(0.08),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      padding: const EdgeInsets.all(10),
                      child: Image.asset(
                        'assets/icons/filter.png',
                        width: 20,
                        height: 20,
                        color: primaryColor,
                        fit: BoxFit.contain,
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
          
          if (_selectedFilters.isNotEmpty)
            Container(
              height: 48,
              color: Colors.white,
              padding: const EdgeInsets.only(bottom: 12),
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: _selectedFilters.entries.expand<Widget>((entry) {
                  final List<FilterItem> items = entry.value;
                  return items.map<Widget>((item) => Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: RawChip(
                      label: Text('${entry.key}: ${item.name}'),
                      labelStyle: const TextStyle(fontSize: 12, color: primaryColor, fontWeight: FontWeight.w600),
                      onDeleted: () {
                        setState(() {
                          _selectedFilters[entry.key]!.remove(item);
                          if (_selectedFilters[entry.key]!.isEmpty) {
                            _selectedFilters.remove(entry.key);
                          }
                        });
                        _loadMembers(refresh: true);
                      },
                      backgroundColor: primaryColor.withOpacity(0.06),
                      deleteIconColor: primaryColor,
                      deleteIcon: const Icon(Icons.cancel, size: 16),
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      side: BorderSide(color: primaryColor.withOpacity(0.1)),
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 0),
                    ),
                  ));
                }).toList().cast<Widget>(),
              ),
            ),

          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 4.0),
            child: Row(
              children: [
                Text(
                  'Total Records: $_totalRecords',
                  style: TextStyle(fontSize: 12, color: Colors.grey.shade600, fontWeight: FontWeight.w500),
                ),
              ],
            ),
          ),

          Expanded(
            child: _isLoading 
              ? const Center(child: CircularProgressIndicator(color: primaryColor))
              : _error != null
                  ? Center(child: Text('Error: $_error'))
                  : RefreshIndicator(
                      onRefresh: () => _loadMembers(refresh: true),
                      color: primaryColor,
                      child: ListView.separated(
                        controller: _scrollController,
                        padding: const EdgeInsets.only(bottom: 20),
                        itemCount: _members.length + (_isLoadingMore ? 1 : 0),
                        separatorBuilder: (context, index) => Divider(
                          color: Colors.grey.shade300,
                          height: 1,
                          indent: 16,
                          endIndent: 16,
                        ),
                        itemBuilder: (context, index) {
                          if (index == _members.length) {
                            return const Padding(
                              padding: EdgeInsets.all(16.0),
                              child: Center(child: CircularProgressIndicator(color: primaryColor)),
                            );
                          }
                          return MemberCard(
                            member: _members[index],
                          );
                        },
                      ),
                    ),
          ),
        ],
      ),
    );
  }
}
























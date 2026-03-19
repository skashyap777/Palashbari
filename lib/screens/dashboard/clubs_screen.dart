import 'package:flutter/material.dart';
import '../../models/entity.dart';
import '../../widgets/user_avatar.dart';
import '../../models/paginated_response.dart';
import '../../services/api_service.dart';
import '../../config/dashboard_config.dart';
import '../member_list_screen.dart';
import '../../widgets/filter_bottom_sheet.dart';
import '../../models/filter_item.dart';

class ClubsScreen extends StatefulWidget {
  const ClubsScreen({super.key});

  @override
  State<ClubsScreen> createState() => _ClubsScreenState();
}

class _ClubsScreenState extends State<ClubsScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  final DashboardIcon _dashboardIcon = DashboardConfig.getIconByTitle('Clubs')!;

  List<Entity> _entities = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _error;
  int _currentPage = 0;
  final int _pageSize = 10;
  int _totalRecords = 0;
  
  final Map<String, List<FilterItem>> _selectedFilters = {};

  @override
  void initState() {
    super.initState();
    _loadEntities();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _searchController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      if (!_isLoadingMore && _entities.length < _totalRecords) {
        _loadMore();
      }
    }
  }

  Future<void> _loadEntities({bool refresh = false}) async {
    if (refresh) {
      setState(() {
        _currentPage = 0;
        _entities.clear();
      });
    }

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final params = Map<String, dynamic>.from(_dashboardIcon.defaultParams ?? {});
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

      final paginatedResponse = PaginatedResponse<Entity>.fromJson(
        response,
        (json) => Entity.fromJson(json),
      );

      if (mounted) {
        setState(() {
          _entities = paginatedResponse.data;
          _totalRecords = paginatedResponse.recordsTotal;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  Future<void> _loadMore() async {
    setState(() {
      _isLoadingMore = true;
      _currentPage++;
    });

    try {
      final params = Map<String, dynamic>.from(_dashboardIcon.defaultParams ?? {});
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

      final paginatedResponse = PaginatedResponse<Entity>.fromJson(
        response,
        (json) => Entity.fromJson(json),
      );

      if (mounted) {
        setState(() {
          _entities.addAll(paginatedResponse.data);
          _isLoadingMore = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoadingMore = false;
        });
      }
    }
  }

  String _getApiKey(String category) {
    if (category == 'gaon_panchayat') return 'gaon_panchayat_id';
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
            _selectedFilters.clear();
            _selectedFilters.addAll(filters);
          });
          _loadEntities(refresh: true);
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
                      onChanged: (value) => _loadEntities(refresh: true),
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
                        _loadEntities(refresh: true);
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
                      onRefresh: () => _loadEntities(refresh: true),
                      color: primaryColor,
                      child: ListView.separated(
                        controller: _scrollController,
                        padding: const EdgeInsets.only(bottom: 20),
                        itemCount: _entities.length + (_isLoadingMore ? 1 : 0),
                        separatorBuilder: (context, index) => Divider(
                          color: Colors.grey.shade300,
                          height: 1,
                          indent: 16,
                          endIndent: 16,
                        ),
                        itemBuilder: (context, index) {
                          if (index == _entities.length) {
                            return const Padding(
                              padding: EdgeInsets.all(16.0),
                              child: Center(child: CircularProgressIndicator(color: primaryColor)),
                            );
                          }
                          final entity = _entities[index];
                          return Card(
                            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            child: ListTile(
                              contentPadding: const EdgeInsets.all(16),
                              title: Text(
                                entity.name,
                                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                              ),
                              subtitle: Column(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const SizedBox(height: 8),
                                  Text('Address: ${entity.address ?? "N/A"}'),
                                  Text('Members: ${entity.memberCount ?? "N/A"}'),
                                ],
                              ),
                              onTap: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => MemberListScreen(
                                      dashboardIcon: _dashboardIcon,
                                      entityId: entity.id,
                                      entityName: entity.name,
                                    ),
                                  ),
                                );
                              },
                            ),
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

























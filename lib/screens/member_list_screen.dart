import 'package:flutter/material.dart';
import '../models/member.dart';
import '../models/paginated_response.dart';
import '../services/api_service.dart';
import '../widgets/member_card.dart';
import '../widgets/filter_bottom_sheet.dart';
import '../config/dashboard_config.dart';
import '../models/filter_item.dart';
import 'dart:convert';

/// Generic member list screen for all dashboard icons
class MemberListScreen extends StatefulWidget {
  final DashboardIcon dashboardIcon;
  final int? entityId;
  final String? entityName;
  final Map<String, dynamic>? extraParams;

  final String? customTitle;

  const MemberListScreen({
    super.key,
    required this.dashboardIcon,
    this.entityId,
    this.entityName,
    this.extraParams,
    this.customTitle,
  });

  @override
  State<MemberListScreen> createState() => _MemberListScreenState();
}

class _MemberListScreenState extends State<MemberListScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  List<Member> _members = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _error;
  
  int _currentPage = 0;
  final int _pageSize = 10;
  int _totalRecords = 0;
  
  final Map<String, List<String>> _selectedIds = {};
  final Map<String, List<FilterItem>> _selectedFilters = {}; // For display chips
  bool _isSearching = false;

  @override
  void initState() {
    super.initState();
    _loadMembers();
    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _searchController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      if (!_isLoadingMore && _members.length < _totalRecords) {
        _loadMore();
      }
    }
  }

  Future<void> _loadMembers({bool refresh = false}) async {
    if (refresh) {
      setState(() {
        _currentPage = 0;
        _members.clear();
      });
    }

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final params = <String, dynamic>{
        ...?widget.dashboardIcon.defaultParams,
        ...?widget.extraParams,
      };

      // Add entity specific ID
      if (widget.entityId != null) {
        if (widget.dashboardIcon.title == 'Morchas') {
          params['morcha_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Schools') {
          params['school_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Clubs') {
          params['club_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Self Help Groups') {
          params['block_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Namghar & Mandirs') {
          params['namghar_and_mandirs_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Organisation') {
          params['organisation_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Beneficiaries') {
          params['block'] = widget.entityId;
        }
      }

      if (widget.dashboardIcon.title == 'Self Help Groups') {
        params['self_help_group_id'] = '';
      }

      // Format filters for API
      // Ensure all expected filter keys are present (even if empty) as some API endpoints require them
      for (var category in widget.dashboardIcon.filterCategories ?? []) {
        params[_getApiKey(category)] = '';
      }

      _selectedFilters.forEach((key, value) {
        if (value.isNotEmpty) {
          params[_getApiKey(key)] = value.map((v) => v.id).join(',');
        }
      });

      final response = await _apiService.fetchPaginatedData(
        endpoint: widget.dashboardIcon.endpoint,
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
    if (_isLoadingMore) return;

    setState(() {
      _isLoadingMore = true;
      _currentPage++;
    });

    try {
      final params = <String, dynamic>{
        ...?widget.dashboardIcon.defaultParams,
        ...?widget.extraParams,
      };

      // Add entity specific ID
      if (widget.entityId != null) {
        if (widget.dashboardIcon.title == 'Morchas') {
          params['morcha_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Schools') {
          params['school_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Clubs') {
          params['club_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Self Help Groups') {
          params['block_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Namghar & Mandirs') {
          params['namghar_and_mandirs_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Organisation') {
          params['organisation_id'] = widget.entityId;
        } else if (widget.dashboardIcon.title == 'Beneficiaries') {
          params['block'] = widget.entityId;
        }
      }

      if (widget.dashboardIcon.title == 'Self Help Groups') {
        params['self_help_group_id'] = '';
      }

      // Format filters for API
      // Ensure all expected filter keys are present (even if empty) as some API endpoints require them
      for (var category in widget.dashboardIcon.filterCategories ?? []) {
        params[_getApiKey(category)] = '';
      }

      _selectedFilters.forEach((key, value) {
        if (value.isNotEmpty) {
          params[_getApiKey(key)] = value.map((v) => v.id).join(',');
        }
      });

      final response = await _apiService.fetchPaginatedData(
        endpoint: widget.dashboardIcon.endpoint,
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
          _members.addAll(paginatedResponse.data);
          _isLoadingMore = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _isLoadingMore = false;
          _currentPage--; // Revert page increment on error
        });
      }
    }
  }

  void _onSearch(String value) {
    // Debounce search
    Future.delayed(const Duration(milliseconds: 500), () {
      if (_searchController.text == value) {
        _loadMembers(refresh: true);
      }
    });
  }

  void _showFilterBottomSheet() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => FilterBottomSheet(
        initialFilters: _selectedIds,
        categoriesToShow: widget.dashboardIcon.filterCategories ?? [],
        onApply: (selectedFilters) {
          setState(() {
            _selectedIds.clear();
            _selectedFilters.clear();
            
            selectedFilters.forEach((key, items) {
              _selectedIds[key] = items.map((item) => item.id).toList();
              _selectedFilters[key] = items;
            });
            _currentPage = 0;
            _members.clear();
          });
          _loadMembers();
        },
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true, // Center the title as in the image
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF00BBA7), size: 28),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          widget.customTitle ?? widget.entityName ?? widget.dashboardIcon.title,
          style: const TextStyle(
            color: Colors.black87,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12.0),
            child: CircleAvatar(
              radius: 20,
              backgroundColor: const Color(0xFF00BBA7),
              child: const Icon(
                Icons.person_outline,
                color: Colors.white,
                size: 24,
              ),
            ),
          ),
        ],
      ),
      body: Column(
        children: [
          // Search and Filter bar
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
            child: Row(
              children: [
                Expanded(
                  child: Container(
                    height: 50,
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(25),
                      border: Border.all(color: Colors.grey.shade300),
                    ),
                    child: TextField(
                      controller: _searchController,
                      onChanged: _onSearch,
                      decoration: InputDecoration(
                        hintText: 'Search...',
                        hintStyle: TextStyle(color: Colors.grey.shade400, fontSize: 16),
                        border: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 14),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                InkWell(
                  onTap: _showFilterBottomSheet,
                  child: const Icon(
                    Icons.filter_alt_outlined,
                    color: Color(0xFF00BBA7),
                    size: 32,
                  ),
                ),
              ],
            ),
          ),

          // Selected Filter Chips
          if (_selectedFilters.values.any((list) => list.isNotEmpty))
            Container(
              height: 50,
              padding: const EdgeInsets.symmetric(vertical: 4),
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: _selectedFilters.entries.expand<Widget>((entry) {
                  final catKey = entry.key;
                  final List<FilterItem> items = entry.value;
                  final catLabel = _getCategoryLabel(catKey);
                  return items.map<Widget>((item) => Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: Chip(
                      backgroundColor: primaryColor.withOpacity(0.1),
                      label: Text(
                        '$catLabel: ${item.name}',
                        style: const TextStyle(fontSize: 12, color: primaryColor, fontWeight: FontWeight.bold),
                      ),
                      deleteIcon: const Icon(Icons.close, size: 14, color: primaryColor),
                      onDeleted: () {
                        setState(() {
                          _selectedFilters[catKey]?.remove(item);
                          _selectedIds[catKey]?.remove(item.id);
                        });
                        _currentPage = 0;
                        _members.clear();
                        _loadMembers();
                      },
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      side: BorderSide(color: primaryColor.withOpacity(0.2)),
                    ),
                  ));
                }).toList().cast<Widget>(),
              ),
            ),

          // Member list container
          Expanded(
            child: Padding(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 16),
              child: Container(
                decoration: BoxDecoration(
                  color: const Color(0xFFEBEBEB),
                  borderRadius: BorderRadius.circular(28),
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(28),
                  child: Column(
                    children: [
                      Expanded(child: _buildBody()),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBody() {
    if (_isLoading && _members.isEmpty) {
      return const Center(
        child: CircularProgressIndicator(
          color: Color(0xFF00BBA7),
        ),
      );
    }

    if (_error != null && _members.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Icon(Icons.error_outline, size: 64, color: Colors.grey),
            const SizedBox(height: 16),
            Text('Error loading data', style: TextStyle(color: Colors.grey.shade600)),
            const SizedBox(height: 16),
            ElevatedButton(
              onPressed: () => _loadMembers(refresh: true),
              style: ElevatedButton.styleFrom(backgroundColor: const Color(0xFF00BBA7)),
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_members.isEmpty) {
      return Center(
        child: Text('No members found', style: TextStyle(color: Colors.grey.shade600)),
      );
    }

    return RefreshIndicator(
      onRefresh: () => _loadMembers(refresh: true),
      color: const Color(0xFF00BBA7),
      child: ListView.separated(
        controller: _scrollController,
        padding: const EdgeInsets.only(bottom: 20), // Added some padding at the bottom
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
              child: Center(child: CircularProgressIndicator(color: Color(0xFF00BBA7))),
            );
          }
          final isKarmiScreen = widget.dashboardIcon.title == 'ASHA Karmi' || 
                             widget.dashboardIcon.title == 'Anganwadi Karmi';
          final isVdpScreen = widget.dashboardIcon.title == 'VDP Members';
          final isBeneficiaryScreen = widget.dashboardIcon.title == 'Beneficiaries';
          final isShgScreen = widget.dashboardIcon.title == 'Self Help Groups';
          
          return MemberCard(
            member: _members[index],
            showAddress: !isKarmiScreen && !isBeneficiaryScreen && !isShgScreen,
            showGeographicInfo: !isKarmiScreen && !isVdpScreen && !isBeneficiaryScreen && !isShgScreen,
            showShgInfo: isShgScreen, // New flag for specific SHG info display
            designationFirst: isVdpScreen,
          );
        },
      ),
    );
  }

  String _getCategoryLabel(String key) {
    switch (key) {
      case 'gaon_panchayat': return 'Gaon Panchayat';
      case 'ward': return 'Ward';
      case 'mandal': return 'Mandal';
      case 'booth': return 'Polling Booth';
      case 'designation': return 'Desig';
      case 'zilla_parishad': return 'Zilla Parishad';
      case 'anchalik_panchayat': return 'Anchalik Panchayat';
      case 'village': return 'Revenue Village';
      case 'school_category': return 'School Category';
      default: return key;
    }
  }

  String _getApiKey(String category) {
    if (category == 'booth') return 'polling_booth';
    if (category == 'zilla_parishad') return 'zilla_parishad_id';
    if (category == 'anchalik_panchayat') return 'anchalik_panchayat_id';
    if (category == 'village') {
      if (widget.dashboardIcon.title == 'Self Help Groups') {
        return 'village_id';
      }
      return 'village';
    }
    if (category == 'school_category') return 'school_type';
    
    if (category == 'gaon_panchayat') {
      if (widget.dashboardIcon.title == 'GP Members' || widget.dashboardIcon.title == 'Self Help Groups') {
        return 'gaon_panchayat_id';
      }
      return 'gaon_panchayat';
    }
    
    if (category == 'ward') {
      if (widget.dashboardIcon.title == 'HSB Team') {
        return 'ward_number';
      }
      return 'ward';
    }
    
    return category;
  }
}

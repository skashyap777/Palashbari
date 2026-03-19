import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/member.dart';
import '../widgets/user_avatar.dart';
import '../models/paginated_response.dart';
import '../services/api_service.dart';
import '../services/api_constants.dart';
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
          // In Android, morchaType is passed from MorchasFragment to MorchasDetailsFragment
          // and used in the API call. We should ensure it's available.
          if (widget.extraParams?['type'] != null) {
            params['type'] = widget.extraParams?['type'];
          }
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
        endpoint: _getEndpoint(),
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
          if (widget.extraParams?['type'] != null) {
            params['type'] = widget.extraParams?['type'];
          }
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
        endpoint: _getEndpoint(),
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
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Color(0xFF1D1D1F), size: 20),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          widget.customTitle ?? widget.entityName ?? widget.dashboardIcon.title,
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
          // Search and Filter bar
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
                    child: TextField(
                      controller: _searchController,
                      onChanged: _onSearch,
                      style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w500),
                      decoration: InputDecoration(
                        hintText: 'Search',
                        hintStyle: TextStyle(color: Colors.grey.shade500, fontSize: 15),
                        prefixIcon: Icon(Icons.search, color: Colors.grey.shade500, size: 20),
                        border: InputBorder.none,
                        contentPadding: const EdgeInsets.symmetric(vertical: 10),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                GestureDetector(
                  onTap: _showFilterBottomSheet,
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
            ),
          ),

          // Selected Filter Chips
          if (_selectedFilters.values.any((list) => list.isNotEmpty))
            Container(
              height: 48,
              color: Colors.white,
              padding: const EdgeInsets.only(bottom: 12),
              child: ListView(
                scrollDirection: Axis.horizontal,
                padding: const EdgeInsets.symmetric(horizontal: 16),
                children: _selectedFilters.entries.expand<Widget>((entry) {
                  final catKey = entry.key;
                  final List<FilterItem> items = entry.value;
                  final catLabel = _getCategoryLabel(catKey);
                  return items.map<Widget>((item) => Padding(
                    padding: const EdgeInsets.only(right: 8.0),
                    child: RawChip(
                      label: Text('$catLabel: ${item.name}'),
                      labelStyle: const TextStyle(fontSize: 12, color: primaryColor, fontWeight: FontWeight.w600),
                      onDeleted: () {
                        setState(() {
                          _selectedFilters[catKey]?.remove(item);
                          _selectedIds[catKey]?.remove(item.id);
                        });
                        _currentPage = 0;
                        _members.clear();
                        _loadMembers();
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

          // Total Records Count
          if (!_isLoading && _members.isNotEmpty)
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
              child: Row(
                children: [
                  Text(
                    '$_totalRecords records found',
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.w500,
                      color: Colors.grey.shade600,
                    ),
                  ),
                ],
              ),
            ),

          // Member list container
          Expanded(
            child: _buildBody(),
          ),
        ],
      ),
    );
  }
  
  String _getEndpoint() {
    switch (widget.dashboardIcon.title) {
      case 'Morchas':            return ApiConstants.morchaMembers;
      case 'Schools':            return ApiConstants.schoolMembers;
      case 'Clubs':              return ApiConstants.clubMembers;
      case 'Mandal Committee':   return ApiConstants.mandalCommittee;
      case 'Namghar & Mandirs':  return ApiConstants.namgharMandirMembers;
      case 'Organisation':       return ApiConstants.organisationMembers;
      case 'Beneficiaries':      return ApiConstants.schemeMembers;
      case 'Self Help Groups':   return ApiConstants.selfHelpGroupMembers;
      default:                   return widget.dashboardIcon.endpoint;
    }
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
        padding: const EdgeInsets.only(bottom: 24, top: 8),
        itemCount: _members.length + (_isLoadingMore ? 1 : 0),
        separatorBuilder: (context, index) => const SizedBox(height: 2),
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
          final isBihuScreen = widget.dashboardIcon.title == 'Bihu Committee' ||
                             widget.dashboardIcon.title == 'Local Business Association' ||
                             widget.dashboardIcon.title == 'Local Mohila Committee';
          final isInfluentialScreen = widget.dashboardIcon.title == 'Influential Persons';
          final isMandalCommittee = widget.dashboardIcon.endpoint == ApiConstants.mandalCommittee;
          
          return MemberCard(
            member: _members[index],
            showAddress: !isKarmiScreen && !isBeneficiaryScreen && !isShgScreen && !isBihuScreen && !isInfluentialScreen && !isMandalCommittee,
            showGeographicInfo: !isKarmiScreen && !isVdpScreen && !isBeneficiaryScreen && !isShgScreen && !isInfluentialScreen && !isMandalCommittee,
            showShgInfo: isShgScreen,
            designationFirst: isVdpScreen || isBihuScreen,
            showInfluentialInfo: isInfluentialScreen,
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

import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/entity.dart';
import '../widgets/user_avatar.dart';
import '../models/paginated_response.dart';
import '../services/api_service.dart';
import '../config/dashboard_config.dart';
import 'member_list_screen.dart';
import '../services/api_constants.dart';
import '../models/filter_item.dart';
import '../widgets/filter_bottom_sheet.dart';
import 'mandal_type_selection_screen.dart';

class EntityListScreen extends StatefulWidget {
  final DashboardIcon dashboardIcon;
  final String? customTitle;

  const EntityListScreen({
    super.key,
    required this.dashboardIcon,
    this.customTitle,
  });

  @override
  State<EntityListScreen> createState() => _EntityListScreenState();
}

class _EntityListScreenState extends State<EntityListScreen> {
  final ApiService _apiService = ApiService();
  final TextEditingController _searchController = TextEditingController();
  final ScrollController _scrollController = ScrollController();

  List<Entity> _entities = [];
  bool _isLoading = false;
  bool _isLoadingMore = false;
  String? _error;
  int _currentPage = 0;
  final int _pageSize = 10;
  int _totalRecords = 0;
  
  final Map<String, List<String>> _selectedIds = {};
  final Map<String, List<FilterItem>> _selectedFilters = {}; // For display chips

  // Special for Namghar & Mandirs
  String _selectedType = 'namghar';

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
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
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
      final params = Map<String, dynamic>.from(widget.dashboardIcon.defaultParams ?? {});
      // Ensure all expected filter keys are present (even if empty) as some API endpoints require them
      for (var category in widget.dashboardIcon.filterCategories ?? []) {
        params[_getApiKey(category)] = '';
      }

      if (widget.dashboardIcon.title == 'Namghar \u0026 Mandirs') {
        params['type'] = _selectedType;
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

      final paginatedResponse = PaginatedResponse<Entity>.fromJson(
        response,
        (json) => Entity.fromJson(json),
      );

      if (mounted) {
        setState(() {
          if (refresh) {
            _entities = paginatedResponse.data;
          } else {
            _entities.addAll(paginatedResponse.data);
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
      final params = Map<String, dynamic>.from(widget.dashboardIcon.defaultParams ?? {});
      // Ensure all expected filter keys are present (even if empty) as some API endpoints require them
      for (var category in widget.dashboardIcon.filterCategories ?? []) {
        params[_getApiKey(category)] = '';
      }

      if (widget.dashboardIcon.title == 'Namghar \u0026 Mandirs') {
        params['type'] = _selectedType;
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
          _currentPage--;
        });
      }
    }
  }

  void _onSearch(String value) {
    _loadEntities(refresh: true);
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
            _entities.clear();
          });
          _loadEntities();
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
          widget.customTitle ?? widget.dashboardIcon.title,
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
                if (widget.dashboardIcon.filterCategories != null && widget.dashboardIcon.filterCategories!.isNotEmpty) ...[
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
              ],
            ),
          ),

          // Namghar / Mandir tab toggle
          if (widget.dashboardIcon.title == 'Namghar & Mandirs')
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 4),
              child: Row(
                children: [
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        if (_selectedType != 'namghar') {
                          setState(() {
                            _selectedType = 'namghar';
                            _currentPage = 0;
                            _entities.clear();
                          });
                          _loadEntities();
                        }
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        decoration: BoxDecoration(
                          color: _selectedType == 'namghar'
                              ? const Color(0xFF00BBA7)
                              : Colors.grey.shade200,
                          borderRadius: BorderRadius.circular(25),
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          'Namghar',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: _selectedType == 'namghar'
                                ? Colors.white
                                : Colors.black54,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 10),
                  Expanded(
                    child: GestureDetector(
                      onTap: () {
                        if (_selectedType != 'mandir') {
                          setState(() {
                            _selectedType = 'mandir';
                            _currentPage = 0;
                            _entities.clear();
                          });
                          _loadEntities();
                        }
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(vertical: 10),
                        decoration: BoxDecoration(
                          color: _selectedType == 'mandir'
                              ? const Color(0xFF00BBA7)
                              : Colors.grey.shade200,
                          borderRadius: BorderRadius.circular(25),
                        ),
                        alignment: Alignment.center,
                        child: Text(
                          'Mandir',
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: _selectedType == 'mandir'
                                ? Colors.white
                                : Colors.black54,
                          ),
                        ),
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
                        _entities.clear();
                        _loadEntities();
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

          // List
          Expanded(
            child: _isLoading && _entities.isEmpty
                 ? const Center(child: CircularProgressIndicator(color: primaryColor))
                : _error != null
                    ? Center(
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('Error: $_error'),
                            ElevatedButton(
                              onPressed: () => _loadEntities(refresh: true),
                              child: const Text('Retry'),
                            ),
                          ],
                        ),
                      )
                    : _entities.isEmpty
                        ? const Center(child: Text('No records found'))
                        : ListView.builder(
                            controller: _scrollController,
                            padding: const EdgeInsets.all(16),
                            itemCount: _entities.length + (_isLoadingMore ? 1 : 0),
                            itemBuilder: (context, index) {
                              if (index == _entities.length) {
                                return const Center(
                                  child: Padding(
                                    padding: EdgeInsets.all(8.0),
                                    child: CircularProgressIndicator(color: primaryColor),
                                  ),
                                );
                              }
                              return _buildEntityCard(_entities[index]);
                            },
                          ),
          ),
        ],
      ),
    );
  }

  Widget _buildEntityCard(Entity entity) {
    const primaryColor = Color(0xFF00BBA7);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        title: Text(
          entity.name,
          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
        ),
        subtitle: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            if (entity.address != null && entity.address!.isNotEmpty)
              Padding(
                padding: const EdgeInsets.only(top: 4.0),
                child: Text(entity.address!, style: TextStyle(color: Colors.grey.shade600)),
              ),
            if (entity.memberCount != null)
              Padding(
                padding: const EdgeInsets.only(top: 4.0),
                child: Text(
                  'Members: ${entity.memberCount}',
                  style: const TextStyle(color: primaryColor, fontWeight: FontWeight.w600),
                ),
              ),
          ],
        ),
        trailing: widget.dashboardIcon.title == 'Namghar & Mandirs'
            ? null
            : const Icon(Icons.chevron_right, color: primaryColor),
        onTap: widget.dashboardIcon.title == 'Namghar & Mandirs'
            ? null
            : () {
          // Special handling for multi-level screens like Beneficiaries or SHG
          if (widget.dashboardIcon.title == 'Beneficiaries' && widget.dashboardIcon.endpoint == ApiConstants.schemes) {
            // Level 1: Schemes -> Go to Blocks
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => EntityListScreen(
                  dashboardIcon: DashboardIcon(
                    id: widget.dashboardIcon.id,
                    title: 'Beneficiaries',
                    iconPath: widget.dashboardIcon.iconPath,
                    endpoint: ApiConstants.blocks,
                    type: DashboardIconType.entityList,
                    filterCategories: [], // No filters at block level
                    defaultParams: {'scheme': entity.id},
                  ),
                  customTitle: entity.name,
                ),
              ),
            );
          } else if (widget.dashboardIcon.title == 'Beneficiaries' && widget.dashboardIcon.endpoint == ApiConstants.blocks) {
            // Level 2: Blocks -> Go to Beneficiary List
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => MemberListScreen(
                  dashboardIcon: DashboardIcon(
                    id: widget.dashboardIcon.id,
                    title: 'Beneficiaries',
                    iconPath: widget.dashboardIcon.iconPath,
                    endpoint: ApiConstants.beneficiary,
                    type: DashboardIconType.memberList,
                    filterCategories: ['gaon_panchayat', 'ward'],
                  ),
                  entityId: entity.id,
                  entityName: entity.name,
                  extraParams: {
                    ...?widget.dashboardIcon.defaultParams, // Carry over scheme_id
                  },
                ),
              ),
            );
          } else if (widget.dashboardIcon.title == 'Self Help Groups' && widget.dashboardIcon.endpoint == ApiConstants.blocks) {
            // Level 2: Blocks -> Go to SHG Member List (SHGDetailsFragment in Android)
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => MemberListScreen(
                  dashboardIcon: DashboardIcon(
                    id: widget.dashboardIcon.id,
                    title: 'Self Help Groups',
                    iconPath: widget.dashboardIcon.iconPath,
                    endpoint: ApiConstants.selfHelpGroupMembers,
                    type: DashboardIconType.memberList,
                    filterCategories: ['gaon_panchayat', 'village'],
                  ),
                  entityId: entity.id,
                  entityName: entity.name,
                  customTitle: entity.name,
                ),
              ),
            );
          } else if (widget.dashboardIcon.title == 'Mandal Committee' && widget.dashboardIcon.endpoint == ApiConstants.mandals) {
            // Level 1: Mandals -> Go to Mandal Type Selection
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => MandalTypeSelectionScreen(
                  dashboardIcon: widget.dashboardIcon,
                  mandalId: entity.id,
                  mandalName: entity.name,
                ),
              ),
            );
          } else {
            // Final Level -> Go to MemberListScreen
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => MemberListScreen(
                  dashboardIcon: DashboardIcon(
                    id: widget.dashboardIcon.id,
                    title: widget.dashboardIcon.title,
                    iconPath: widget.dashboardIcon.iconPath,
                    endpoint: _getMembersEndpoint(),
                    type: DashboardIconType.memberList,
                    filterCategories: (widget.dashboardIcon.title == 'Morchas' || 
                                       widget.dashboardIcon.title == 'Schools' || 
                                       widget.dashboardIcon.title == 'Clubs' ||
                                       widget.dashboardIcon.title == 'Namghar & Mandirs')
                        ? ['designation'] 
                        : widget.dashboardIcon.filterCategories,
                  ),
                  entityId: entity.id,
                  entityName: entity.name,
                  extraParams: {
                    ...?widget.dashboardIcon.defaultParams,
                  },
                ),
              ),
            );
          }
        },
      ),
    );
  }

  String _getMembersEndpoint() {
    switch (widget.dashboardIcon.title) {
      case 'Morchas':
        return ApiConstants.morchaMembers;
      case 'Schools':
        return ApiConstants.schoolMembers;
      case 'Clubs':
        return ApiConstants.clubMembers;
      case 'Self Help Groups':
        return ApiConstants.selfHelpGroupMembers;
      case 'Namghar & Mandirs':
        return ApiConstants.namgharMandirMembers;
      case 'Beneficiaries':
        return ApiConstants.schemeMembers;
      case 'Organization':
        return ApiConstants.organisationMembers;
      default:
        return widget.dashboardIcon.endpoint;
    }
  }

  String _getCategoryLabel(String key) {
    switch (key) {
      case 'gaon_panchayat': return 'Gaon Panchayat';
      case 'ward': return 'Ward';
      case 'mandal': return 'Mandal';
      case 'booth': return 'Polling Booth';
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
    if (category == 'village') return 'village';
    if (category == 'school_category') return 'school_type';

    if (category == 'gaon_panchayat') {
      if (widget.dashboardIcon.title == 'GP Members') {
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

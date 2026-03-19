import 'package:flutter/material.dart';
import '../models/filter_item.dart';
import '../services/api_service.dart';
import '../services/api_constants.dart';

class FilterBottomSheet extends StatefulWidget {
  final Map<String, List<String>> initialFilters;
  final List<String> categoriesToShow;
  final Function(Map<String, List<FilterItem>>) onApply;

  const FilterBottomSheet({
    super.key,
    required this.initialFilters,
    required this.categoriesToShow,
    required this.onApply,
  });

  @override
  State<FilterBottomSheet> createState() => _FilterBottomSheetState();
}

class _FilterBottomSheetState extends State<FilterBottomSheet> {
  final ApiService _apiService = ApiService();
  
  String _selectedCategory = '';
  final Map<String, List<FilterItem>> _categoryData = {};
  final Map<String, List<String>> _selectedIds = {};
  final TextEditingController _searchController = TextEditingController();
  String _searchText = '';
  bool _isLoading = true;
  List<Map<String, String>> _categories = [];

  @override
  void initState() {
    super.initState();
    
    // Define all possible categories
    final allCategories = [
      {'id': 'gaon_panchayat', 'label': 'Gaon Panchayat'},
      {'id': 'ward', 'label': 'Ward'},
      {'id': 'mandal', 'label': 'Mandal'},
      {'id': 'booth', 'label': 'Polling Booth'},
      {'id': 'designation', 'label': 'Designation'},
      {'id': 'village', 'label': 'Revenue Village'},
      {'id': 'zilla_parishad', 'label': 'Zilla Parishad'},
      {'id': 'anchalik_panchayat', 'label': 'Anchalik Panchayat'},
      {'id': 'school_category', 'label': 'School Category'},
    ];

    // Filter to only show requested ones
    _categories = allCategories.where((cat) => widget.categoriesToShow.contains(cat['id'])).toList();
    
    // Default selected category
    if (_categories.isNotEmpty) {
      _selectedCategory = _categories.first['id']!;
    }

    // Copy initial filters
    widget.initialFilters.forEach((key, value) {
      _selectedIds[key] = List<String>.from(value);
    });
    
    // Ensure categories exist in the map
    for (var cat in _categories) {
      _selectedIds.putIfAbsent(cat['id']!, () => []);
    }
    
    _loadAllFilters();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  Future<void> _loadAllFilters() async {
    setState(() => _isLoading = true);
    try {
      final List<Future> futures = [];
      final List<String> loadedKeys = [];

      if (widget.categoriesToShow.contains('gaon_panchayat')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.gaonPanchayat, length: 200));
        loadedKeys.add('gaon_panchayat');
      }
      if (widget.categoriesToShow.contains('ward')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.wards, length: 200));
        loadedKeys.add('ward');
      }
      if (widget.categoriesToShow.contains('mandal')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.mandals, length: 200));
        loadedKeys.add('mandal');
      }
      if (widget.categoriesToShow.contains('booth')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.pollingBooths, length: 500));
        loadedKeys.add('booth');
      }
      if (widget.categoriesToShow.contains('designation')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.designationMaster, length: 200));
        loadedKeys.add('designation');
      }
      if (widget.categoriesToShow.contains('village')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.villages, length: 500));
        loadedKeys.add('village');
      }
      if (widget.categoriesToShow.contains('zilla_parishad')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.zillaParishad, length: 200));
        loadedKeys.add('zilla_parishad');
      }
      if (widget.categoriesToShow.contains('anchalik_panchayat')) {
        futures.add(_apiService.fetchPaginatedData(endpoint: ApiConstants.anchalikPanchayat, length: 200));
        loadedKeys.add('anchalik_panchayat');
      }
      if (widget.categoriesToShow.contains('school_category')) {
        futures.add(Future.value({
          'data': [
            {'value': 'HS', 'name': 'HS'},
            {'value': 'HSS', 'name': 'HSS'},
            {'value': 'LPS', 'name': 'LPS'},
            {'value': 'UPS', 'name': 'UPS'},
          ]
        }));
        loadedKeys.add('school_category');
      }

      final results = await Future.wait(futures);
      
      if (mounted) {
        setState(() {
          for (int i = 0; i < loadedKeys.length; i++) {
            final key = loadedKeys[i];
            _categoryData[key] = (results[i]['data'] as List?)
                ?.map((json) => FilterItem.fromJson(json))
                .toList() ?? [];
          }
          _isLoading = false;
        });
      }
    } catch (e) {
      print('DEBUG: Error in _loadAllFilters: $e');
      if (mounted) {
        setState(() => _isLoading = false);
      }
    }
  }

  void _toggleSelection(String id) {
    setState(() {
      final currentList = _selectedIds[_selectedCategory]!;
      if (currentList.contains(id)) {
        currentList.remove(id);
      } else {
        currentList.add(id);
      }
    });
  }

  void _clearFilters() {
    setState(() {
      _selectedIds.forEach((key, value) => value.clear());
    });
  }

  Map<String, List<FilterItem>> _getGroupedItems() {
    var items = _categoryData[_selectedCategory] ?? [];
    
    if (_searchText.isNotEmpty) {
      items = items.where((item) => 
        item.name.toLowerCase().contains(_searchText.toLowerCase())
      ).toList();
    }

    if (items.isEmpty) return {};

    final grouped = <String, List<FilterItem>>{
      'A - F': [],
      'G - M': [],
      'N - Z': [],
    };

    for (var item in items) {
      final firstLetter = item.name.isNotEmpty ? item.name[0].toUpperCase() : '';
      if (RegExp(r'[A-F]').hasMatch(firstLetter)) {
        grouped['A - F']!.add(item);
      } else if (RegExp(r'[G-M]').hasMatch(firstLetter)) {
        grouped['G - M']!.add(item);
      } else if (RegExp(r'[N-Z]').hasMatch(firstLetter) || firstLetter.isEmpty || RegExp(r'[0-9]').hasMatch(firstLetter)) {
        grouped['N - Z']!.add(item);
      }
    }
    
    // Remove empty groups
    grouped.removeWhere((key, value) => value.isEmpty);
    return grouped;
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Container(
      height: MediaQuery.of(context).size.height * 0.85,
      decoration: const BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Grab Handle
          Center(
            child: Container(
              margin: const EdgeInsets.only(top: 12),
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey.shade300,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          
          // Header
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 16, 12, 12),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Filters',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.w700,
                    color: Colors.black,
                  ),
                ),
                IconButton(
                  icon: Container(
                    padding: const EdgeInsets.all(4),
                    decoration: BoxDecoration(
                      color: Colors.grey.shade100,
                      shape: BoxShape.circle,
                    ),
                    child: const Icon(Icons.close, size: 20, color: Colors.black54),
                  ),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
          ),
          const Divider(height: 1, thickness: 0.5),

          // Main Content
          Expanded(
            child: Row(
              children: [
                // Sidebar
                Container(
                  width: 130,
                  decoration: BoxDecoration(
                    color: Colors.grey.shade50,
                    border: Border(right: BorderSide(color: Colors.grey.shade200, width: 0.5)),
                  ),
                  child: ListView.builder(
                    itemCount: _categories.length,
                    itemBuilder: (context, index) {
                      final cat = _categories[index];
                      final isSelected = _selectedCategory == cat['id'];
                      return InkWell(
                        onTap: () {
                          setState(() {
                            _selectedCategory = cat['id']!;
                            _searchController.clear();
                            _searchText = '';
                          });
                        },
                        child: AnimatedContainer(
                          duration: const Duration(milliseconds: 200),
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 18,
                          ),
                          decoration: BoxDecoration(
                            color: isSelected ? Colors.white : Colors.transparent,
                          ),
                          child: Row(
                            children: [
                              Expanded(
                                child: Text(
                                  cat['label']!,
                                  style: TextStyle(
                                    fontSize: 13,
                                    fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                                    color: isSelected ? primaryColor : Colors.black54,
                                  ),
                                ),
                              ),
                              if (isSelected)
                                Container(
                                  width: 3,
                                  height: 16,
                                  decoration: BoxDecoration(
                                    color: primaryColor,
                                    borderRadius: BorderRadius.circular(2),
                                  ),
                                ),
                            ],
                          ),
                        ),
                      );
                    },
                  ),
                ),

                // Options
                Expanded(
                  child: _isLoading
                      ? const Center(child: CircularProgressIndicator(strokeWidth: 2, color: primaryColor))
                      : _buildOptionsList(),
                ),
              ],
            ),
          ),

          // Bottom Buttons
          Container(
            padding: const EdgeInsets.fromLTRB(20, 16, 20, 40),
            decoration: BoxDecoration(
              color: Colors.white,
              border: Border(top: BorderSide(color: Colors.grey.shade200, width: 0.5)),
            ),
            child: Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 50,
                    child: TextButton(
                      onPressed: _clearFilters,
                      style: TextButton.styleFrom(
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                          side: BorderSide(color: Colors.grey.shade300),
                        ),
                      ),
                      child: Text(
                        'Clear',
                        style: TextStyle(
                          fontSize: 15, 
                          fontWeight: FontWeight.w600,
                          color: Colors.grey.shade700
                        ),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: SizedBox(
                    height: 50,
                    child: ElevatedButton(
                      onPressed: () {
                        final result = <String, List<FilterItem>>{};
                        _selectedIds.forEach((catKey, ids) {
                          if (ids.isNotEmpty) {
                            final allItems = _categoryData[catKey] ?? [];
                            result[catKey] = allItems.where((item) => ids.contains(item.id)).toList();
                          }
                        });
                        widget.onApply(result);
                        Navigator.pop(context);
                      },
                      style: ElevatedButton.styleFrom(
                        backgroundColor: primaryColor,
                        foregroundColor: Colors.white,
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                      child: const Text(
                        'Show Results',
                        style: TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildOptionsList() {
    final groupedItems = _getGroupedItems();
    const primaryColor = Color(0xFF00BBA7);
    
    return Column(
      children: [
        Padding(
          padding: const EdgeInsets.all(16.0),
          child: Container(
            height: 40,
            decoration: BoxDecoration(
              color: Colors.grey.shade100,
              borderRadius: BorderRadius.circular(10),
            ),
            child: TextField(
              controller: _searchController,
              onChanged: (value) => setState(() => _searchText = value),
              style: const TextStyle(fontSize: 14),
              decoration: InputDecoration(
                hintText: 'Search...',
                hintStyle: TextStyle(fontSize: 14, color: Colors.grey.shade400),
                prefixIcon: Icon(Icons.search, size: 18, color: Colors.grey.shade400),
                border: InputBorder.none,
                contentPadding: const EdgeInsets.symmetric(vertical: 10),
              ),
            ),
          ),
        ),
        Expanded(
          child: groupedItems.isEmpty
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(Icons.search_off_rounded, size: 48, color: Colors.grey.shade200),
                      const SizedBox(height: 12),
                      Text(
                        'No options found',
                        style: TextStyle(color: Colors.grey.shade400, fontSize: 13),
                      ),
                    ],
                  ),
                )
              : ListView(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  children: groupedItems.entries.map((entry) {
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Padding(
                          padding: const EdgeInsets.only(bottom: 12.0, top: 8.0),
                          child: Text(
                            entry.key,
                            style: TextStyle(
                              fontSize: 11,
                              fontWeight: FontWeight.w700,
                              color: Colors.grey.shade400,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ),
                        Wrap(
                          spacing: 8,
                          runSpacing: 8,
                          children: entry.value.map((item) {
                            final isSelected = _selectedIds[_selectedCategory]!.contains(item.id);
                            return _buildFilterChip(item, isSelected);
                          }).toList(),
                        ),
                        const SizedBox(height: 24),
                      ],
                    );
                  }).toList(),
                ),
        ),
      ],
    );
  }

  Widget _buildFilterChip(FilterItem item, bool isSelected) {
    const primaryColor = Color(0xFF00BBA7);
    
    return InkWell(
      onTap: () => _toggleSelection(item.id),
      splashColor: primaryColor.withOpacity(0.05),
      highlightColor: Colors.transparent,
      borderRadius: BorderRadius.circular(8),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? primaryColor.withOpacity(0.05) : Colors.white,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(
            color: isSelected ? primaryColor : Colors.grey.shade200,
            width: 1,
          ),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: 18,
              height: 18,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                  color: isSelected ? primaryColor : Colors.grey.shade300,
                  width: 1.5,
                ),
                color: isSelected ? primaryColor : Colors.transparent,
              ),
              child: isSelected
                  ? const Center(child: Icon(Icons.check, size: 10, color: Colors.white))
                  : null,
            ),
            const SizedBox(width: 10),
            Flexible(
              child: Text(
                item.name,
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                  color: isSelected ? primaryColor : Colors.black87,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

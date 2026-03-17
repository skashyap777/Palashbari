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
        futures.add(_apiService.fetchData(endpoint: ApiConstants.schoolType));
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
    final items = _categoryData[_selectedCategory] ?? [];
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
        borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Padding(
            padding: const EdgeInsets.fromLTRB(28, 24, 28, 16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Text(
                  'Filters',
                  style: TextStyle(
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                    color: Color(0xFF333333),
                  ),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => Navigator.pop(context),
                ),
              ],
            ),
          ),
          const Divider(height: 1),

          // Main Content
          Expanded(
            child: Row(
              children: [
                // Sidebar
                Container(
                  width: 120,
                  color: const Color(0xFFF5F5F5),
                  child: ListView.builder(
                    itemCount: _categories.length,
                    itemBuilder: (context, index) {
                      final cat = _categories[index];
                      final isSelected = _selectedCategory == cat['id'];
                      return InkWell(
                        onTap: () => setState(() => _selectedCategory = cat['id']!),
                        child: Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 20,
                          ),
                          decoration: BoxDecoration(
                            color: isSelected ? Colors.white : Colors.transparent,
                            border: isSelected
                                ? const Border(
                                    left: BorderSide(color: primaryColor, width: 4),
                                  )
                                : null,
                          ),
                          child: Text(
                            cat['label']!,
                            style: TextStyle(
                              fontSize: 14,
                              fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
                              color: isSelected ? primaryColor : Colors.grey.shade600,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                ),

                // Options
                Expanded(
                  child: _isLoading
                      ? const Center(child: CircularProgressIndicator(color: primaryColor))
                      : _buildOptionsList(),
                ),
              ],
            ),
          ),

          // Bottom Buttons
          Container(
            padding: const EdgeInsets.fromLTRB(24, 16, 24, 32),
            decoration: BoxDecoration(
              color: Colors.white,
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, -5),
                ),
              ],
            ),
            child: Row(
              children: [
                Expanded(
                  child: SizedBox(
                    height: 54,
                    child: ElevatedButton(
                      onPressed: _clearFilters,
                      style: ElevatedButton.styleFrom(
                        backgroundColor: const Color(0xFFE0E0E0),
                        foregroundColor: Colors.grey.shade700,
                        elevation: 0,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(27),
                        ),
                      ),
                      child: const Text(
                        'Clear',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  flex: 2,
                  child: SizedBox(
                    height: 54,
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
                          borderRadius: BorderRadius.circular(27),
                        ),
                      ),
                      child: const Text(
                        'Show Results',
                        style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
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
    if (groupedItems.isEmpty) {
      return const Center(child: Text('No options found'));
    }

    return ListView(
      padding: const EdgeInsets.all(20),
      children: groupedItems.entries.map((entry) {
        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.only(bottom: 12.0, top: 12.0),
              child: Text(
                entry.key,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: Colors.grey.shade500,
                  letterSpacing: 1.2,
                ),
              ),
            ),
            Wrap(
              spacing: 10,
              runSpacing: 10,
              children: entry.value.map((item) {
                final isSelected = _selectedIds[_selectedCategory]!.contains(item.id);
                return _buildFilterChip(item, isSelected);
              }).toList(),
            ),
            const SizedBox(height: 16),
          ],
        );
      }).toList(),
    );
  }

  Widget _buildFilterChip(FilterItem item, bool isSelected) {
    const primaryColor = Color(0xFF00BBA7);
    
    return InkWell(
      onTap: () => _toggleSelection(item.id),
      borderRadius: BorderRadius.circular(20),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(25),
          border: Border.all(
            color: isSelected ? primaryColor : Colors.grey.shade300,
            width: 1,
          ),
          boxShadow: isSelected ? [
            BoxShadow(
              color: primaryColor.withOpacity(0.1),
              blurRadius: 4,
              offset: const Offset(0, 2),
            )
          ] : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Container(
              width: 20,
              height: 20,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                border: Border.all(
                  color: isSelected ? primaryColor : Colors.grey.shade400,
                  width: 2,
                ),
                color: isSelected ? primaryColor : Colors.transparent,
              ),
              child: isSelected
                  ? const Icon(Icons.check, size: 12, color: Colors.white)
                  : null,
            ),
            const SizedBox(width: 8),
            Flexible(
              child: Text(
                item.name,
                style: TextStyle(
                  fontSize: 13,
                  fontWeight: isSelected ? FontWeight.bold : FontWeight.w500,
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

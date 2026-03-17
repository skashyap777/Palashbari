class FilterItem {
  final String id;
  final String name;

  FilterItem({
    required this.id,
    required this.name,
  });

  factory FilterItem.fromJson(Map<String, dynamic> json) {
    return FilterItem(
      id: (json['id'] ?? json['value'] ?? json['name'] ?? '0').toString(),
      name: (json['name'] ?? 
             json['value'] ??
             json['type'] ??
             json['village_name'] ?? 
             json['gaon_panchayat_name'] ?? 
             json['mandal_name'] ?? 
             json['booth_name'] ??
             json['polling_booth_name'] ??
             json['number'] ?? 
             'Item ${json['id']}')?.toString() ?? '',
    );
  }
}

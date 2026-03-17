/// Model for organizations, clubs, schools, etc.
class Entity {
  final int id;
  final String name;
  final String? address;
  final int? memberCount;
  final String? type;
  final String? category;

  Entity({
    required this.id,
    required this.name,
    this.address,
    this.memberCount,
    this.type,
    this.category,
  });

  factory Entity.fromJson(Map<String, dynamic> json) {
    return Entity(
      id: json['id'] ?? 0,
      name: json['name'] ?? json['morcha_name'] ?? json['organisation_name'] ?? json['club_name'] ?? '',
      address: json['address'],
      memberCount: json['member_count'],
      type: json['type'],
      category: json['category'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'address': address,
      'member_count': memberCount,
      'type': type,
      'category': category,
    };
  }
}

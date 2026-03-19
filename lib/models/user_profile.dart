/// Model for user profile
class UserProfile {
  final int id;
  final String name;
  final String? email;
  final String? phoneNumber;
  final String? photoUrl;
  final String? role;
  final int active;
  final String? createdAt;
  final String? updatedAt;

  UserProfile({
    required this.id,
    required this.name,
    this.email,
    this.phoneNumber,
    this.photoUrl,
    this.role,
    required this.active,
    this.createdAt,
    this.updatedAt,
  });

  factory UserProfile.fromJson(Map<String, dynamic> json) {
    return UserProfile(
      id: json['id'] ?? 0,
      name: json['name'] ?? '',
      email: json['email'],
      phoneNumber: json['phone_number'],
      photoUrl: _buildPhotoUrl(json['photo_url']),
      role: json['role'],
      active: json['active'] ?? 1,
      createdAt: json['created_at'],
      updatedAt: json['updated_at'],
    );
  }

  static String? _buildPhotoUrl(dynamic photoUrl) {
    if (photoUrl == null || photoUrl.toString().isEmpty || photoUrl.toString().toLowerCase() == 'null') {
      return null;
    }
    final url = photoUrl.toString();
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    return 'https://palasbari.h24x7.in/$url';
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'email': email,
      'phone_number': phoneNumber,
      'photo_url': photoUrl,
      'role': role,
      'active': active,
      'created_at': createdAt,
      'updated_at': updatedAt,
    };
  }

  UserProfile copyWith({
    int? id,
    String? name,
    String? email,
    String? phoneNumber,
    String? photoUrl,
    String? role,
    int? active,
    String? createdAt,
    String? updatedAt,
  }) {
    return UserProfile(
      id: id ?? this.id,
      name: name ?? this.name,
      email: email ?? this.email,
      phoneNumber: phoneNumber ?? this.phoneNumber,
      photoUrl: photoUrl ?? this.photoUrl,
      role: role ?? this.role,
      active: active ?? this.active,
      createdAt: createdAt ?? this.createdAt,
      updatedAt: updatedAt ?? this.updatedAt,
    );
  }
}

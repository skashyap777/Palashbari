/// Model for member data (used across multiple endpoints)
class Member {
  final int id;
  final String name;
  final String? designation;
  final int? designationId;
  final String phoneCode;
  final String phoneNumber;
  final String? email;
  final String? address;
  final String? ward;
  final String? gaonPanchayat;
  final String? mandal;
  final String? pollingBooth;
  final String? village;
  final String? pristhaNo;
  final int active;
  final String? createdAt;
  final String? updatedAt;
  final String? selfHelpGroup;
  final String? addedByName;

  Member({
    required this.id,
    required this.name,
    this.designation,
    this.designationId,
    required this.phoneCode,
    required this.phoneNumber,
    this.email,
    this.address,
    this.ward,
    this.gaonPanchayat,
    this.mandal,
    this.pollingBooth,
    this.village,
    this.pristhaNo,
    this.selfHelpGroup,
    required this.active,
    this.createdAt,
    this.updatedAt,
    this.addedByName,
  });

  factory Member.fromJson(Map<String, dynamic> json) {
    return Member(
      id: json['id'] ?? 0,
      name: json['name'] ?? json['pramukh_name'] ?? json['pradhan_name'] ?? '',
      designation: json['designation_name'] ?? json['type'] ?? (json['designation'] is String ? json['designation'] : null),
      designationId: json['designation'] is int ? json['designation'] : null,
      phoneCode: json['phone_code'] ?? '+91',
      phoneNumber: json['phone_number'] ?? '',
      email: json['email'],
      address: json['address'],
      ward: json['ward_name'] ?? json['ward_names'] ?? json['ward_number'] ?? json['ward'],
      gaonPanchayat: json['gaon_panchayat_names'] ?? json['gaon_panchayat_name'] ?? json['panchayat_name'] ?? json['gaon_panchayat'],
      mandal: json['mandal_name'] ?? json['mandal'],
      pollingBooth: json['polling_booth_name'] ?? json['polling_booth'],
      village: json['village_names'] ?? json['village_name'] ?? json['vill_name'] ?? json['village'],
      pristhaNo: json['pristha_nos']?.toString(),
      selfHelpGroup: json['self_help_group_name'],
      active: json['active'] ?? 1,
      createdAt: json['created_at'],
      updatedAt: json['updated_at'],
      addedByName: json['user_name'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'designation_name': designation,
      'designation': designationId,
      'phone_code': phoneCode,
      'phone_number': phoneNumber,
      'email': email,
      'address': address,
      'ward': ward,
      'gaon_panchayat': gaonPanchayat,
      'mandal': mandal,
      'polling_booth': pollingBooth,
      'village': village,
      'active': active,
      'created_at': createdAt,
      'updated_at': updatedAt,
      'user_name': addedByName,
    };
  }

  String get fullPhoneNumber {
    if (phoneNumber.startsWith('+')) return phoneNumber;
    if (phoneNumber.startsWith(phoneCode.replaceAll('+', ''))) return '+$phoneNumber';
    return '$phoneCode$phoneNumber';
  }
  
  String get whatsappUrl {
    final cleanPhone = fullPhoneNumber.replaceAll('+', '');
    return 'https://wa.me/$cleanPhone';
  }
  
  String get callUrl => 'tel:$fullPhoneNumber';
}

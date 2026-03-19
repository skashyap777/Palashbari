import '../services/api_constants.dart';

/// Dashboard icon configuration
class DashboardIcon {
  final int id;
  final String title;
  final String iconPath;
  final String endpoint;
  final DashboardIconType type;
  final Map<String, dynamic>? defaultParams;
  final List<String>? filterCategories;

  const DashboardIcon({
    required this.id,
    required this.title,
    required this.iconPath,
    required this.endpoint,
    required this.type,
    this.defaultParams,
    this.filterCategories,
  });
}

/// Type of dashboard icon (determines the UI flow)
enum DashboardIconType {
  memberList,      // Direct member list (e.g., HSB Team, District Committee)
  entityList,      // List of entities first, then members (e.g., Schools, Clubs)
  search,          // Search-based (e.g., All Members)
  special,         // Special handling (e.g., Election Results)
}

/// All dashboard icons configuration
class DashboardConfig {
  static const List<DashboardIcon> icons = [
    // Row 1
    DashboardIcon(
      id: 0,
      title: 'HSB Team',
      iconPath: 'assets/icons/hsb_team.png',
      endpoint: ApiConstants.hsbTeam,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat', 'ward'],
    ),
    DashboardIcon(
      id: 1,
      title: 'District Committee',
      iconPath: 'assets/icons/district_committee.png',
      endpoint: ApiConstants.districtCommittee,
      type: DashboardIconType.memberList,
      filterCategories: ['designation'],
    ),
    DashboardIcon(
      id: 2,
      title: 'Mandal Committee',
      iconPath: 'assets/icons/mandal_committee.png',
      endpoint: ApiConstants.mandals,
      type: DashboardIconType.entityList,
      filterCategories: [],
    ),
    DashboardIcon(
      id: 3,
      title: 'Morchas',
      iconPath: 'assets/icons/morchas.png',
      endpoint: ApiConstants.morchas,
      type: DashboardIconType.entityList,
      defaultParams: {'type': 'district-morcha'},
      filterCategories: ['mandal', 'designation'],
    ),
    
    // Row 2
    DashboardIcon(
      id: 4,
      title: 'Shakti Kendra',
      iconPath: 'assets/icons/shakti_kendra.png',
      endpoint: ApiConstants.shaktiKendra,
      type: DashboardIconType.memberList,
      filterCategories: ['mandal', 'booth'],
    ),
    DashboardIcon(
      id: 5,
      title: 'Pristha Ahbayak',
      iconPath: 'assets/icons/pristha_ahbayak.png',
      endpoint: ApiConstants.pristhaPramukhs,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat', 'booth'],
    ),
    DashboardIcon(
      id: 6,
      title: 'ZPC Members',
      iconPath: 'assets/icons/zpc_members.png',
      endpoint: ApiConstants.zillaParishadMembers,
      type: DashboardIconType.memberList,
      filterCategories: ['zilla_parishad'],
    ),
    DashboardIcon(
      id: 7,
      title: 'AP Members',
      iconPath: 'assets/icons/ap_members.png',
      endpoint: ApiConstants.anchalikPanchayatMembers,
      type: DashboardIconType.memberList,
      filterCategories: ['anchalik_panchayat'],
    ),
    
    // Row 3
    DashboardIcon(
      id: 8,
      title: 'GP Members',
      iconPath: 'assets/icons/gp_members.png',
      endpoint: ApiConstants.gaonPanchayatMembers,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat', 'ward'],
    ),
    DashboardIcon(
      id: 9,
      title: 'Village Pradhan',
      iconPath: 'assets/icons/village_pradhan.png',
      endpoint: ApiConstants.villagePradhans,
      type: DashboardIconType.memberList,
      filterCategories: ['village'],
    ),
    DashboardIcon(
      id: 10,
      title: 'ASHA Karmi',
      iconPath: 'assets/icons/asha_karmi.png',
      endpoint: ApiConstants.karmis,
      type: DashboardIconType.memberList,
      defaultParams: {'category': 'asha', 'type': 'karmi'},
      filterCategories: ['gaon_panchayat', 'booth'],
    ),
    DashboardIcon(
      id: 11,
      title: 'Anganwadi Karmi',
      iconPath: 'assets/icons/anganwadi_karmi.png',
      endpoint: ApiConstants.karmis,
      type: DashboardIconType.memberList,
      defaultParams: {'category': 'anganwadi', 'type': 'supervisor'},
      filterCategories: ['gaon_panchayat', 'booth'],
    ),
    
    // Row 4
    DashboardIcon(
      id: 12,
      title: 'VDP Members',
      iconPath: 'assets/icons/vdp_members.png',
      endpoint: ApiConstants.vdpGroup,
      type: DashboardIconType.memberList,
      filterCategories: ['village'],
    ),
    DashboardIcon(
      id: 13,
      title: 'Beneficiaries',
      iconPath: 'assets/icons/beneficiaries.png',
      endpoint: ApiConstants.schemes, // Use schemes list first
      type: DashboardIconType.entityList,
      filterCategories: [], // Filters are only applied at the final member list level in this flow
    ),
    DashboardIcon(
      id: 14,
      title: 'Self Help Groups',
      iconPath: 'assets/icons/self_help_groups.png',
      endpoint: ApiConstants.blocks, // SHG flow often starts with blocks in Android
      type: DashboardIconType.entityList,
      filterCategories: ['gaon_panchayat', 'village'],
    ),
    DashboardIcon(
      id: 15,
      title: 'Schools',
      iconPath: 'assets/icons/schools.png',
      endpoint: ApiConstants.schools,
      type: DashboardIconType.entityList,
      filterCategories: ['school_category', 'gaon_panchayat'],
    ),
    
    // Row 5
    DashboardIcon(
      id: 16,
      title: 'Clubs',
      iconPath: 'assets/icons/clubs.png',
      endpoint: ApiConstants.clubs,
      type: DashboardIconType.entityList,
      filterCategories: ['gaon_panchayat'],
    ),
    DashboardIcon(
      id: 17,
      title: 'Namghar & Mandirs',
      iconPath: 'assets/icons/namghars_mandirs.png',
      endpoint: ApiConstants.namgharMandir,
      type: DashboardIconType.entityList,
      filterCategories: ['gaon_panchayat'],
    ),
    DashboardIcon(
      id: 18,
      title: 'Bihu Committee',
      iconPath: 'assets/icons/bihu_committee.png',
      endpoint: ApiConstants.bihuCommittee,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat'],
    ),
    DashboardIcon(
      id: 19,
      title: 'Senior Citizen',
      iconPath: 'assets/icons/senior_citizen.png',
      endpoint: ApiConstants.seniorCitizen,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat'],
    ),
    
    // Row 6
    DashboardIcon(
      id: 20,
      title: 'Influential Persons',
      iconPath: 'assets/icons/influential_persons.png',
      endpoint: ApiConstants.influentialPerson,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat'],
    ),
    DashboardIcon(
      id: 21,
      title: 'Local Business Association',
      iconPath: 'assets/icons/local_business_association.png',
      endpoint: ApiConstants.localBusinessAssociation,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat'],
    ),
    DashboardIcon(
      id: 22,
      title: 'Election Results',
      iconPath: 'assets/icons/election_results.png',
      endpoint: ApiConstants.electionResult,
      type: DashboardIconType.special,
    ),
    DashboardIcon(
      id: 23,
      title: 'Local Mohila Committee',
      iconPath: 'assets/icons/local_mohila_committee.png',
      endpoint: ApiConstants.localMohilaCommittee,
      type: DashboardIconType.memberList,
      filterCategories: ['gaon_panchayat'],
    ),
  ];

  /// Get icon by ID
  static DashboardIcon? getIconById(int id) {
    try {
      return icons.firstWhere((icon) => icon.id == id);
    } catch (e) {
      return null;
    }
  }

  /// Get icon by title
  static DashboardIcon? getIconByTitle(String title) {
    try {
      return icons.firstWhere((icon) => icon.title == title);
    } catch (e) {
      return null;
    }
  }
}

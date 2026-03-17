/// API Constants for Palasbari Application
class ApiConstants {
  // Base URL
  static const String baseUrl = 'https://palasbari.h24x7.in/api';

  // Authentication Endpoints
  static const String login = '/login';
  static const String refreshToken = '/refresh-token';
  static const String logout = '/logout';
  
  // Profile Endpoints
  static const String profile = '/profile';
  static const String updateProfile = '/update-profile';
  static const String changePassword = '/change-password';
  
  // Dashboard
  static const String dashboardCounts = '/dashboard-counts';
  
  // Organization Structure
  static const String districtCommittee = '/district-committee/read';
  static const String districtCell = '/district-cell/read';
  static const String morchaMembers = '/morcha-members/read';
  static const String mandalCommittee = '/mandal-committe/read';
  static const String boothCommittee = '/booth-committe/read';
  static const String shaktiKendra = '/sakti-kendra/read';
  static const String pristhaPramukhs = '/pristha-pramukhs/read';
  static const String wardMembers = '/ward-members/read';
  static const String anchalikPanchayatMembers = '/anchalik-panchayat-members/read';
  static const String zillaParishadMembers = '/zilla-parishad-members/read';
  static const String gaonPanchayatMembers = '/gaon-panchayat-members/read';
  
  // Masters
  static const String mandals = '/mandals/read';
  static const String gaonPanchayat = '/gaon-panchayat/read';
  static const String pollingBooths = '/polling-booths/read';
  static const String villages = '/villages/read';
  static const String schemes = '/schemes/read';
  static const String morchas = '/morchas/read';
  static const String designationMaster = '/designation-master/read';
  static const String wards = '/wards/read';
  static const String zillaParishad = '/zilla-parishad/read';
  static const String anchalikPanchayat = '/anchalik-panchayat/read';
  static const String blocks = '/blocks/read';
  
  // Beneficiary & Groups
  static const String beneficiary = '/beneficiary/read';
  static const String selfHelpGroups = '/self-help-groups/read';
  static const String selfHelpGroupMembers = '/self-help-group-members/read';
  
  // Clubs
  static const String clubs = '/clubs/read';
  static const String clubMembers = '/club-members/read';
  
  // Karmi (ASHA & Anganwadi)
  static const String karmis = '/karmis/read';
  
  // Schools
  static const String schools = '/schools/read';
  static const String schoolMembers = '/school-members/read';
  static const String schoolType = '/school-type';
  
  // Namghar & Mandirs
  static const String namgharMandir = '/namghar-n-mandir/read';
  static const String namgharMandirMembers = '/namghar-mandir-members/read';
  
  // Important Persons
  static const String seniorCitizen = '/important-persons/read';
  static const String influentialPerson = '/influential-person/read';
  
  // Organization
  static const String organisation = '/organisation/read';
  static const String organisationMembers = '/organisation-memebers/read';
  
  // Village & Community
  static const String villagePradhans = '/village-pradhans/read';
  static const String hsbTeam = '/hsb-team/read';
  static const String vdpGroup = '/vdp-group/read';
  static const String bihuCommittee = '/bihu-committee/read';
  static const String localMohilaCommittee = '/local-mohila-committee/read';
  static const String localBusinessAssociation = '/local-business-association/read';
  
  // Search & Utility
  static const String searchPhone = '/search/phone-datatable';
  static const String users = '/users/read';
  static const String electionResult = '/election-result/read';
  static const String schemeMembers = '/scheme-members/read';
  
  // Helper endpoints
  static const String gaonPanchayatVillages = '/gaon-panchayat-villages';
  static const String gaonPanchayatWiseWards = '/gaon-panchayat-wise-wards';
  static const String gaonPanchayatPollingBooths = '/gaon-panchayat-polling-booths';
  static const String getGPPBByMandal = '/get-gaon-panchayat-polling-booth-of-selected-mandal';
}

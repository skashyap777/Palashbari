import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'settings_screen.dart';
import 'config/dashboard_config.dart';
import 'screens/member_list_screen.dart';
import 'screens/entity_list_screen.dart';
import 'screens/election_results_screen.dart';
import 'screens/dashboard/hsb_team_screen.dart';
import 'screens/dashboard/district_committee_screen.dart';
import 'screens/dashboard/mandal_committee_screen.dart';
import 'screens/dashboard/morchas_screen.dart';
import 'screens/dashboard/shakti_kendra_screen.dart';
import 'screens/dashboard/pristha_abhayak_screen.dart';
import 'screens/dashboard/zpc_members_screen.dart';
import 'screens/dashboard/ap_members_screen.dart';
import 'screens/dashboard/gp_members_screen.dart';
import 'screens/dashboard/village_pradhan_screen.dart';
import 'screens/dashboard/asha_karmi_screen.dart';
import 'screens/dashboard/anganwadi_karmi_screen.dart';
import 'screens/dashboard/vdp_members_screen.dart';
import 'screens/dashboard/beneficiaries_screen.dart';
import 'screens/dashboard/self_help_groups_screen.dart';
import 'screens/dashboard/schools_screen.dart';
import 'screens/dashboard/clubs_screen.dart';
import 'screens/dashboard/namghar_mandirs_screen.dart';
import 'screens/dashboard/bihu_committee_screen.dart';
import 'screens/dashboard/senior_citizen_screen.dart';
import 'screens/dashboard/influential_person_screen.dart';
import 'screens/dashboard/local_business_association_screen.dart';
import 'screens/dashboard/local_mohila_committee_screen.dart';
import 'providers/auth_provider.dart';
import 'widgets/user_avatar.dart';

class HomeScreen extends StatelessWidget {
  const HomeScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      body: SafeArea(
        child: Consumer<AuthProvider>(
          builder: (context, authProvider, child) {
            final userName = authProvider.userProfile?.name ?? '';
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Premium Top Header
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 24, 20, 8),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            userName.isNotEmpty ? 'Welcome, $userName' : 'Welcome back',
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w500,
                              color: Color(0xFF8E8E93),
                            ),
                          ),
                          const Text(
                            'Dashboard',
                            style: TextStyle(
                              fontSize: 32,
                              fontWeight: FontWeight.w800,
                              color: Color(0xFF1D1D1F),
                              letterSpacing: -1,
                            ),
                          ),
                        ],
                      ),
                      GestureDetector(
                        onTap: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => const SettingsScreen(),
                            ),
                          );
                        },
                        child: Container(
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            boxShadow: [
                              BoxShadow(
                                color: Colors.black.withOpacity(0.1),
                                blurRadius: 8,
                                offset: const Offset(0, 4),
                              ),
                            ],
                          ),
                          child: const UserAvatar(radius: 22),
                        ),
                      ),
                    ],
                  ),
                ),

                const SizedBox(height: 8),

                const SizedBox(height: 8),

                // Grid of Menu Items
                Expanded(
                  child: GridView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 24),
                    gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: 3,
                      crossAxisSpacing: 10,
                      mainAxisSpacing: 10,
                      childAspectRatio: 0.85,
                    ),
                    itemCount: DashboardConfig.icons.length,
                    itemBuilder: (context, index) {
                      final icon = DashboardConfig.icons[index];
                      return MenuCard(dashboardIcon: icon);
                    },
                  ),
                ),
              ],
            );
          },
        ),
      ),
    );
  }
}

class MenuCard extends StatelessWidget {
  final DashboardIcon dashboardIcon;

  const MenuCard({
    super.key,
    required this.dashboardIcon,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: () {
        // ... navigation logic remains the same ...
        if (dashboardIcon.title == 'Election Results') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const ElectionResultsScreen()));
        } else if (dashboardIcon.title == 'HSB Team') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const HSBTeamScreen()));
        } else if (dashboardIcon.title == 'District Committee') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const DistrictCommitteeScreen()));
        } else if (dashboardIcon.title == 'Mandal Committee') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const MandalCommitteeScreen()));
        } else if (dashboardIcon.title == 'Morchas') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const MorchasScreen()));
        } else if (dashboardIcon.title == 'GP Members') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const GPMembersScreen()));
        } else if (dashboardIcon.title == 'Shakti Kendra') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const ShaktiKendraScreen()));
        } else if (dashboardIcon.title == 'Pristha Ahbayak') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const PristhaAbhayakScreen()));
        } else if (dashboardIcon.title == 'ZPC Members') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const ZPCMembersScreen()));
        } else if (dashboardIcon.title == 'AP Members') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const APMembersScreen()));
        } else if (dashboardIcon.title == 'Village Pradhan') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const VillagePradhanScreen()));
        } else if (dashboardIcon.title == 'ASHA Karmi') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const AshaKarmiScreen()));
        } else if (dashboardIcon.title == 'Anganwadi Karmi') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const AnganwadiKarmiScreen()));
        } else if (dashboardIcon.title == 'VDP Members') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const VDPMembersScreen()));
        } else if (dashboardIcon.title == 'Beneficiaries') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const BeneficiariesScreen()));
        } else if (dashboardIcon.title == 'Senior Citizen') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const SeniorCitizenScreen()));
        } else if (dashboardIcon.title == 'Bihu Committee') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const BihuCommitteeScreen()));
        } else if (dashboardIcon.title == 'Influential Persons') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const InfluentialPersonScreen()));
        } else if (dashboardIcon.title == 'Self Help Groups') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const SelfHelpGroupsScreen()));
        } else if (dashboardIcon.title == 'Schools') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const SchoolsScreen()));
        } else if (dashboardIcon.title == 'Clubs') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const ClubsScreen()));
        } else if (dashboardIcon.title == 'Namghar & Mandirs') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const NamgharMandirsScreen()));
        } else if (dashboardIcon.title == 'Local Business Association') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const LocalBusinessAssociationScreen()));
        } else if (dashboardIcon.title == 'Local Mohila Committee') {
          Navigator.push(context, MaterialPageRoute(builder: (context) => const LocalMohilaCommitteeScreen()));
        } else if (dashboardIcon.type == DashboardIconType.entityList) {
          Navigator.push(context, MaterialPageRoute(builder: (context) => EntityListScreen(dashboardIcon: dashboardIcon)));
        } else {
          Navigator.push(context, MaterialPageRoute(builder: (context) => MemberListScreen(dashboardIcon: dashboardIcon)));
        }
      },
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(20),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.03),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: const Color(0xFFF2F2F7),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Image.asset(
                dashboardIcon.iconPath,
                width: 48,
                height: 48,
                fit: BoxFit.contain,
              ),
            ),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4.0),
              child: Text(
                dashboardIcon.title,
                textAlign: TextAlign.center,
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w700,
                  color: Color(0xFF1D1D1F),
                  height: 1.1,
                  letterSpacing: -0.2,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}


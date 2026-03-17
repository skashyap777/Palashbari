import 'package:flutter/material.dart';
import '../config/dashboard_config.dart';
import 'member_list_screen.dart';
import '../services/api_constants.dart';

class MandalTypeSelectionScreen extends StatelessWidget {
  final DashboardIcon dashboardIcon;
  final int mandalId;
  final String mandalName;

  const MandalTypeSelectionScreen({
    super.key,
    required this.dashboardIcon,
    required this.mandalId,
    required this.mandalName,
  });

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    final List<Map<String, dynamic>> committeeTypes = [
      {'id': 1, 'name': '3 Members Committee'},
      {'id': 2, 'name': 'Mandal Main Committee'},
      {'id': 3, 'name': 'Ahbayak Committee'},
    ];

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: primaryColor, size: 28),
          onPressed: () => Navigator.pop(context),
        ),
        title: Column(
          children: [
            Text(
              dashboardIcon.title,
              style: const TextStyle(
                color: Colors.black87,
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            Text(
              mandalName,
              style: TextStyle(
                color: Colors.grey.shade600,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: committeeTypes.length,
        itemBuilder: (context, index) {
          final type = committeeTypes[index];
          return Card(
            margin: const EdgeInsets.only(bottom: 12),
            elevation: 2,
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: ListTile(
              contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              title: Text(
                type['name'],
                style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
              ),
              trailing: const Icon(Icons.chevron_right, color: primaryColor),
              onTap: () {
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => MemberListScreen(
                      dashboardIcon: DashboardIcon(
                        id: dashboardIcon.id,
                        title: type['name'],
                        iconPath: dashboardIcon.iconPath,
                        endpoint: ApiConstants.mandalCommittee,
                        type: DashboardIconType.memberList,
                        filterCategories: ['gaon_panchayat', 'designation'],
                      ),
                      extraParams: {
                        'mandal_id': mandalId,
                        'committee_type': type['id'],
                      },
                    ),
                  ),
                );
              },
            ),
          );
        },
      ),
    );
  }
}

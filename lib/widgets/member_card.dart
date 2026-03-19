import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/member.dart';

/// Member card widget matching Figma design
class MemberCard extends StatelessWidget {
  final Member member;
  final VoidCallback? onTap;
  final bool showAddress;
  final bool showGeographicInfo;
  final bool showShgInfo;
  final bool designationFirst;
  final bool showInfluentialInfo;

  const MemberCard({
    super.key,
    required this.member,
    this.onTap,
    this.showAddress = true,
    this.showGeographicInfo = true,
    this.showShgInfo = false,
    this.designationFirst = false,
    this.showInfluentialInfo = false,
  });

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    try {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } catch (e) {
      debugPrint('Could not launch $url: $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.03),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // Member Info
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // Header Section (Name + Category)
                      if (member.committeeName != null && member.committeeName!.isNotEmpty) ...[
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                          decoration: BoxDecoration(
                            color: primaryColor.withOpacity(0.08),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            member.committeeName!.toUpperCase(),
                            style: const TextStyle(
                              fontSize: 9,
                              fontWeight: FontWeight.w800,
                              color: primaryColor,
                              letterSpacing: 0.5,
                            ),
                          ),
                        ),
                        const SizedBox(height: 10),
                      ],
                      
                      Text(
                        member.name,
                        style: const TextStyle(
                          fontSize: 16,
                          fontWeight: FontWeight.w700,
                          color: Color(0xFF1D1D1F),
                          letterSpacing: -0.3,
                        ),
                      ),
                      
                      const SizedBox(height: 6),

                      // Basic Info (Phone & Designation)
                      ..._buildPhoneAndDesignation(),
                      
                      const SizedBox(height: 12),

                      // Additional Info Section (Geographic/Address/SHG)
                      _buildInfoSection(),
                    ],
                  ),
                ),
                
                const SizedBox(width: 8),

                // Action Buttons
                Column(
                  children: [
                    _buildActionButton(
                      iconPath: 'assets/icons/whatsapp.png',
                      onTap: () => _launchUrl(member.whatsappUrl),
                      color: const Color(0xFF25D366),
                    ),
                    const SizedBox(height: 10),
                    _buildActionButton(
                      iconPath: 'assets/icons/phone.png',
                      onTap: () => _launchUrl(member.callUrl),
                      color: primaryColor,
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildInfoSection() {
    List<String> infoItems = [];

    if (showInfluentialInfo) {
      if (member.gaonPanchayat != null && member.gaonPanchayat!.toLowerCase() != 'null') {
        infoItems.add(member.gaonPanchayat!);
      }
      if (member.pollingBooth != null && member.pollingBooth!.toLowerCase() != 'null') {
        infoItems.add(member.pollingBooth!);
      }
    } else if (showGeographicInfo) {
      if (member.gaonPanchayat != null && member.gaonPanchayat!.toLowerCase() != 'null') {
        infoItems.add(member.gaonPanchayat!);
      }
      if (member.ward != null && member.ward!.toLowerCase() != 'null') {
        infoItems.add(member.ward!.toLowerCase().contains('ward') ? member.ward! : 'Ward ${member.ward}');
      }
      if (member.pollingBooth != null && member.pollingBooth!.toLowerCase() != 'null') {
        infoItems.add(member.pollingBooth!);
      }
      if (member.village != null && member.village!.toLowerCase() != 'null') {
        infoItems.add(member.village!);
      }
    } else if (showShgInfo) {
       if (member.gaonPanchayat != null && member.gaonPanchayat!.toLowerCase() != 'null') {
        infoItems.add(member.gaonPanchayat!);
      }
       if (member.village != null && member.village!.toLowerCase() != 'null') {
        infoItems.add(member.village!);
      }
       if (member.selfHelpGroup != null && member.selfHelpGroup!.toLowerCase() != 'null') {
        infoItems.add(member.selfHelpGroup!);
      }
    }

    if (showAddress && member.address != null && member.address!.toLowerCase() != 'null' && member.address!.isNotEmpty) {
      infoItems.add(member.address!);
    }

    if (infoItems.isEmpty) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: const Color(0xFFF9F9F9),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.grey.shade100, width: 0.5),
      ),
      width: double.infinity,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: infoItems.map((item) => Padding(
          padding: const EdgeInsets.only(bottom: 4.0),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.only(top: 6.0, right: 8.0),
                child: Container(
                  width: 3.5,
                  height: 3.5,
                  decoration: BoxDecoration(
                    color: Colors.grey.shade300,
                    shape: BoxShape.circle,
                  ),
                ),
              ),
              Expanded(
                child: Text(
                  item,
                  style: TextStyle(
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    color: Colors.grey.shade600,
                    height: 1.3,
                  ),
                ),
              ),
            ],
          ),
        )).toList(),
      ),
    );
  }

  Widget _buildActionButton({
    required String iconPath,
    required VoidCallback onTap,
    required Color color,
  }) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Container(
          width: 42,
          height: 42,
          decoration: BoxDecoration(
            color: color.withOpacity(0.08),
            borderRadius: BorderRadius.circular(12),
          ),
          padding: const EdgeInsets.all(10),
          child: Image.asset(
            iconPath,
            fit: BoxFit.contain,
          ),
        ),
      ),
    );
  }

  List<Widget> _buildPhoneAndDesignation() {
    final phoneWidget = Text(
      member.fullPhoneNumber,
      style: TextStyle(
        fontSize: 13,
        color: Colors.grey.shade500,
        fontFamily: 'Roboto',
        fontWeight: FontWeight.w500,
      ),
    );

    Widget? designationWidget;
    if (member.designation != null && 
        member.designation!.isNotEmpty && 
        member.designation!.toLowerCase() != 'null' &&
        member.designation!.toUpperCase() != 'N/A') {
      designationWidget = Text(
        member.designation!,
        style: TextStyle(
          fontSize: 12,
          color: const Color(0xFF00BBA7),
          fontFamily: 'Inter',
          fontWeight: FontWeight.w600,
        ),
      );
    }

    if (designationFirst && designationWidget != null) {
      return [
        designationWidget,
        const SizedBox(height: 2),
        phoneWidget,
      ];
    } else {
      return [
        phoneWidget,
        if (designationWidget != null) ...[
          const SizedBox(height: 2),
          designationWidget,
        ],
      ];
    }
  }
}

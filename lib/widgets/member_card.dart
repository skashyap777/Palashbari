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

  const MemberCard({
    super.key,
    required this.member,
    this.onTap,
    this.showAddress = true,
    this.showGeographicInfo = true,
    this.showShgInfo = false,
    this.designationFirst = false,
  });

  Future<void> _launchUrl(String url) async {
    final uri = Uri.parse(url);
    if (await canLaunchUrl(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    }
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return InkWell(
      onTap: onTap,
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 20.0, horizontal: 16.0),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.center,
          children: [
            // Left side - Member info
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Name
                  Text(
                    member.name,
                    style: const TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600, // Medium
                      color: Colors.black,
                      fontFamily: 'Roboto',
                    ),
                  ),
                  const SizedBox(height: 7),
                  
                  // Phone and Designation in custom order if needed
                  ..._buildPhoneAndDesignation(),

                  // Geographic and extra info
                  if (showGeographicInfo) ...[
                    // GP
                    if (member.gaonPanchayat != null && 
                        member.gaonPanchayat!.isNotEmpty && 
                        member.gaonPanchayat!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.gaonPanchayat!,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black,
                          fontFamily: 'Roboto',
                        ),
                      ),
                    ],

                    // Ward
                    if (member.ward != null && 
                        member.ward!.isNotEmpty && 
                        member.ward!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.ward!.toLowerCase().contains('ward') 
                          ? member.ward! 
                          : 'Ward no. ${member.ward}',
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black,
                          fontFamily: 'Roboto',
                        ),
                      ),
                    ],

                    // Booth
                    if (member.pollingBooth != null && 
                        member.pollingBooth!.isNotEmpty && 
                        member.pollingBooth!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.pollingBooth!,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black,
                          fontFamily: 'Roboto',
                        ),
                      ),
                    ],

                    // Mandal
                    if (member.mandal != null && 
                        member.mandal!.isNotEmpty && 
                        member.mandal!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.mandal!,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black,
                          fontFamily: 'Roboto',
                        ),
                      ),
                    ],

                    // Village
                    if (member.village != null && 
                        member.village!.isNotEmpty && 
                        member.village!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.village!,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.black,
                          fontFamily: 'Roboto',
                        ),
                      ),
                    ],
                  ],

                  // SHG Specific Info (GP, Village, Group)
                  if (showShgInfo) ...[
                    // GP
                    if (member.gaonPanchayat != null && member.gaonPanchayat!.isNotEmpty && member.gaonPanchayat!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.gaonPanchayat!,
                        style: const TextStyle(fontSize: 12, color: Colors.black, fontFamily: 'Roboto'),
                      ),
                    ],
                    // Village
                    if (member.village != null && member.village!.isNotEmpty && member.village!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.village!,
                        style: const TextStyle(fontSize: 12, color: Colors.black, fontFamily: 'Roboto'),
                      ),
                    ],
                    // SHG Name (Group)
                    if (member.selfHelpGroup != null && member.selfHelpGroup!.isNotEmpty && member.selfHelpGroup!.toLowerCase() != 'null') ...[
                      const SizedBox(height: 3),
                      Text(
                        member.selfHelpGroup!,
                        style: const TextStyle(fontSize: 12, color: Colors.black, fontFamily: 'Roboto', fontWeight: FontWeight.w500),
                      ),
                    ],
                  ],

                  // Address
                  if (showAddress && 
                      member.address != null && 
                      member.address!.isNotEmpty && 
                      member.address!.toLowerCase() != 'null') ...[
                    const SizedBox(height: 3),
                    Text(
                      member.address!,
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.black,
                        fontFamily: 'Roboto',
                      ),
                    ),
                  ],

                  // Pristha No.
                  if (showGeographicInfo &&
                      member.pristhaNo != null && 
                      member.pristhaNo!.isNotEmpty && 
                      member.pristhaNo!.toLowerCase() != 'null') ...[
                    const SizedBox(height: 3),
                    Text(
                      'Pristha No. ${member.pristhaNo}',
                      style: const TextStyle(
                        fontSize: 12,
                        color: Colors.black,
                        fontFamily: 'Roboto',
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            
            // Right side - Action buttons
            Row(
              children: [
                // WhatsApp button
                _buildActionButton(
                  icon: Icons.chat_bubble, // More filled like WhatsApp bubble
                  onTap: () => _launchUrl(member.whatsappUrl),
                  color: primaryColor,
                ),
                
                const SizedBox(width: 12),
                
                // Call button
                _buildActionButton(
                  icon: Icons.phone, // Filled phone icon
                  onTap: () => _launchUrl(member.callUrl),
                  color: primaryColor,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required VoidCallback onTap,
    required Color color,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(10),
      child: Container(
        width: 44,
        height: 38,
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(10),
        ),
        child: Icon(
          icon,
          color: Colors.white,
          size: 20,
        ),
      ),
    );
  }

  List<Widget> _buildPhoneAndDesignation() {
    final phoneWidget = Text(
      member.fullPhoneNumber,
      style: const TextStyle(
        fontSize: 12,
        color: Colors.black,
        fontFamily: 'Roboto',
      ),
    );

    Widget? designationWidget;
    if (member.designation != null && 
        member.designation!.isNotEmpty && 
        member.designation!.toLowerCase() != 'null') {
      designationWidget = Text(
        member.designation!,
        style: const TextStyle(
          fontSize: 12,
          color: Colors.black,
          fontFamily: 'Roboto',
          fontWeight: FontWeight.w500,
        ),
      );
    }

    if (designationFirst && designationWidget != null) {
      return [
        designationWidget,
        const SizedBox(height: 3),
        phoneWidget,
      ];
    } else {
      return [
        phoneWidget,
        if (designationWidget != null) ...[
          const SizedBox(height: 3),
          designationWidget,
        ],
      ];
    }
  }
}

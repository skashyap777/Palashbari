import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'profile_screen.dart';
import 'about_screen.dart';
import 'change_password_screen.dart';
import 'providers/auth_provider.dart';
import 'login_screen.dart';
import 'widgets/user_avatar.dart';

class SettingsScreen extends StatefulWidget {
  const SettingsScreen({super.key});

  @override
  State<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends State<SettingsScreen> {
  bool _notificationsEnabled = true;

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Scaffold(
      backgroundColor: const Color(0xFFF2F2F7), // iOS Grouped Background
      appBar: AppBar(
        backgroundColor: Colors.white,
        surfaceTintColor: Colors.white,
        elevation: 0,
        centerTitle: true,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back_ios_new, color: Color(0xFF1D1D1F), size: 20),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text(
          'Settings',
          style: TextStyle(
            color: Color(0xFF1D1D1F),
            fontSize: 17,
            fontWeight: FontWeight.w600,
            letterSpacing: -0.5,
          ),
        ),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            const SizedBox(height: 24),
            
            // Profile Section
            Container(
              margin: const EdgeInsets.symmetric(horizontal: 16),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Consumer<AuthProvider>(
                builder: (context, authProvider, child) {
                  final user = authProvider.userProfile;
                  return Row(
                    children: [
                      const UserAvatar(radius: 32),
                      const SizedBox(width: 16),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              user?.name ?? 'User Name',
                              style: const TextStyle(
                                fontSize: 20,
                                fontWeight: FontWeight.w700,
                                color: Color(0xFF1D1D1F),
                                letterSpacing: -0.5,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              user?.email ?? user?.phoneNumber ?? 'Email/Phone',
                              style: TextStyle(
                                fontSize: 13,
                                color: Colors.grey.shade600,
                                letterSpacing: -0.2,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  );
                },
              ),
            ),

            const SizedBox(height: 32),

            // General Section
            _buildSectionHeader('General'),
            _buildGroupedSection([
              _buildMenuItem(
                iconPath: 'assets/icons/user_svgrepo_com.png',
                iconColor: primaryColor,
                title: 'Edit Profile',
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (context) => const ProfileScreen())),
              ),
              _buildMenuItem(
                iconPath: 'assets/icons/password_minimalistic_input_svgrepo_com.png',
                iconColor: const Color(0xFF5856D6), // iOS Purple
                title: 'Change Password',
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (context) => const ChangePasswordScreen())),
              ),
              _buildMenuItemWithToggle(
                iconPath: 'assets/icons/bell.png',
                iconColor: const Color(0xFFFF9500), // iOS Orange
                title: 'Notifications',
                value: _notificationsEnabled,
                onChanged: (value) => setState(() => _notificationsEnabled = value),
              ),
            ]),

            const SizedBox(height: 24),

            // Information Section
            _buildSectionHeader('Information'),
            _buildGroupedSection([
              _buildMenuItem(
                iconPath: 'assets/icons/info.png',
                iconColor: const Color(0xFF007AFF), // iOS Blue
                title: 'About the App',
                onTap: () => Navigator.push(context, MaterialPageRoute(builder: (context) => const AboutScreen())),
              ),
              _buildMenuItem(
                iconPath: 'assets/icons/location.png',
                iconColor: const Color(0xFF32D74B), // iOS Green
                title: 'Privacy Policy',
                onTap: () {},
              ),
              _buildMenuItem(
                iconPath: 'assets/icons/support.png',
                iconColor: const Color(0xFF64D2FF), // iOS Light Blue
                title: 'Help & Support',
                onTap: () {},
              ),
            ]),

            const SizedBox(height: 32),

            // Logout Group
            _buildGroupedSection([
              _buildMenuItem(
                iconPath: 'assets/icons/logout.png',
                iconColor: const Color(0xFFFF3B30), // iOS Red
                title: 'Logout',
                textColor: const Color(0xFFFF3B30),
                showChevron: false,
                onTap: () => _showLogoutDialog(context),
              ),
            ]),

            const SizedBox(height: 48),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(28, 0, 24, 8),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          title.toUpperCase(),
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w500,
            color: Colors.grey.shade600,
            letterSpacing: 0.5,
          ),
        ),
      ),
    );
  }

  Widget _buildGroupedSection(List<Widget> children) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Column(
        children: children.asMap().entries.map((entry) {
          final index = entry.key;
          final widget = entry.value;
          return Column(
            children: [
              widget,
              if (index < children.length - 1)
                Padding(
                  padding: const EdgeInsets.only(left: 56.0),
                  child: Divider(height: 1, color: Colors.grey.shade200),
                ),
            ],
          );
        }).toList(),
      ),
    );
  }

  Widget _buildMenuItem({
    required String iconPath,
    required Color iconColor,
    required String title,
    required VoidCallback onTap,
    Color textColor = const Color(0xFF1D1D1F),
    bool showChevron = true,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 14.0),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(6),
              decoration: BoxDecoration(
                color: iconColor.withOpacity(0.1),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Image.asset(
                iconPath,
                width: 20,
                height: 20,
              ),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Text(
                title,
                style: TextStyle(
                  fontSize: 16,
                  color: textColor,
                  fontWeight: FontWeight.w500,
                  letterSpacing: -0.2,
                ),
              ),
            ),
            if (showChevron)
              Icon(
                Icons.arrow_forward_ios_rounded,
                color: Colors.grey.shade300,
                size: 14,
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildMenuItemWithToggle({
    required String iconPath,
    required Color iconColor,
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: iconColor.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Image.asset(
              iconPath,
              width: 20,
              height: 20,
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Text(
              title,
              style: const TextStyle(
                fontSize: 16,
                color: Color(0xFF1D1D1F),
                fontWeight: FontWeight.w500,
                letterSpacing: -0.2,
              ),
            ),
          ),
          Transform.scale(
            scale: 0.8,
            child: Switch.adaptive(
              value: value,
              onChanged: onChanged,
              activeColor: const Color(0xFF32D74B), // iOS Green
            ),
          ),
        ],
      ),
    );
  }



  void _showLogoutDialog(BuildContext context) {
    showDialog(
      context: context,
      builder: (BuildContext dialogContext) {
        return AlertDialog(
          title: const Text('Logout'),
          content: const Text('Are you sure you want to logout?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(dialogContext),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () async {
                Navigator.pop(dialogContext); // Close dialog
                
                // Show loading indicator
                showDialog(
                  context: context,
                  barrierDismissible: false,
                  builder: (BuildContext context) {
                    return const Center(
                      child: CircularProgressIndicator(
                        color: Color(0xFF00BBA7),
                      ),
                    );
                  },
                );

                // Perform logout
                final authProvider = Provider.of<AuthProvider>(context, listen: false);
                await authProvider.logout();

                // Close loading indicator
                if (mounted) {
                  Navigator.pop(context);
                  
                  // Navigate to login screen and clear all previous routes
                  Navigator.pushAndRemoveUntil(
                    context,
                    MaterialPageRoute(builder: (context) => const LoginScreen()),
                    (route) => false,
                  );
                }
              },
              child: const Text(
                'Logout',
                style: TextStyle(color: Colors.red),
              ),
            ),
          ],
        );
      },
    );
  }
}

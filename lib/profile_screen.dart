import 'dart:io';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:image_picker/image_picker.dart';
import 'providers/auth_provider.dart';
import 'widgets/user_avatar.dart';

class ProfileScreen extends StatefulWidget {
  const ProfileScreen({super.key});

  @override
  State<ProfileScreen> createState() => _ProfileScreenState();
}

class _ProfileScreenState extends State<ProfileScreen> {
  late TextEditingController _nameController;
  late TextEditingController _phoneController;
  late TextEditingController _emailController;
  String? _imagePath;
  final ImagePicker _picker = ImagePicker();

  @override
  void initState() {
    super.initState();
    final user = Provider.of<AuthProvider>(context, listen: false).userProfile;
    _nameController = TextEditingController(text: user?.name ?? '');
    _phoneController = TextEditingController(text: user?.phoneNumber ?? '');
    _emailController = TextEditingController(text: user?.email ?? '');
  }

  Future<void> _pickImage() async {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          ListTile(
            leading: const Icon(Icons.camera_alt),
            title: const Text('Camera'),
            onTap: () async {
              Navigator.pop(context);
              final XFile? image = await _picker.pickImage(source: ImageSource.camera);
              if (image != null) {
                setState(() => _imagePath = image.path);
              }
            },
          ),
          ListTile(
            leading: const Icon(Icons.photo_library),
            title: const Text('Gallery'),
            onTap: () async {
              Navigator.pop(context);
              final XFile? image = await _picker.pickImage(source: ImageSource.gallery);
              if (image != null) {
                setState(() => _imagePath = image.path);
              }
            },
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    return Scaffold(
      backgroundColor: Colors.white,
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.black87),
          onPressed: () => Navigator.pop(context),
        ),
        title: const Text(
          'Edit Profile',
          style: TextStyle(
            color: Colors.black87,
            fontSize: 18,
            fontWeight: FontWeight.w600,
          ),
        ),
        centerTitle: true,
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 8.0),
            child: Row(
              children: [
                const UserAvatar(),
              ],
            ),
          ),
        ],
      ),
      body: Consumer<AuthProvider>(
        builder: (context, authProvider, child) {
          return SingleChildScrollView(
            padding: const EdgeInsets.all(24.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                const SizedBox(height: 20),
                
                // Profile Picture
                Stack(
                  children: [
                    Container(
                      width: 120,
                      height: 120,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        border: Border.all(
                          color: primaryColor,
                          width: 3,
                        ),
                      ),
                      child: ClipOval(
                        child: _imagePath != null
                            ? Image.file(
                                File(_imagePath!),
                                fit: BoxFit.cover,
                              )
                            : authProvider.userProfile?.photoUrl != null
                                ? Image.network(
                                    authProvider.userProfile!.photoUrl!,
                                    fit: BoxFit.cover,
                                    errorBuilder: (context, error, stackTrace) =>
                                        Image.asset('assets/Logo.png', fit: BoxFit.cover),
                                  )
                                : Image.asset(
                                    'assets/Logo.png',
                                    fit: BoxFit.cover,
                                  ),
                      ),
                    ),
                    Positioned(
                      bottom: 0,
                      right: 0,
                      child: GestureDetector(
                        onTap: _pickImage,
                        child: Container(
                          width: 36,
                          height: 36,
                          decoration: BoxDecoration(
                            color: primaryColor,
                            shape: BoxShape.circle,
                            border: Border.all(
                              color: Colors.white,
                              width: 3,
                            ),
                          ),
                          child: const Icon(
                            Icons.edit,
                            color: Colors.white,
                            size: 18,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 40),

                // Name Field
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 4, bottom: 8),
                      child: Text(
                        'Name',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    TextField(
                      controller: _nameController,
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: Colors.grey.shade50,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 16,
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: primaryColor),
                        ),
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 24),

                // Phone Number Field
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 4, bottom: 8),
                      child: Text(
                        'Phone Number',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    TextField(
                      controller: _phoneController,
                      keyboardType: TextInputType.phone,
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: Colors.grey.shade50,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 16,
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: primaryColor),
                        ),
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 24),

                // Email Field
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(left: 4, bottom: 8),
                      child: Text(
                        'Email',
                        style: TextStyle(
                          fontSize: 14,
                          color: Colors.grey,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ),
                    TextField(
                      controller: _emailController,
                      keyboardType: TextInputType.emailAddress,
                      decoration: InputDecoration(
                        filled: true,
                        fillColor: Colors.grey.shade50,
                        contentPadding: const EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 16,
                        ),
                        border: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        enabledBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: BorderSide.none,
                        ),
                        focusedBorder: OutlineInputBorder(
                          borderRadius: BorderRadius.circular(12),
                          borderSide: const BorderSide(color: primaryColor),
                        ),
                      ),
                    ),
                  ],
                ),

                const SizedBox(height: 40),

                // Update Button
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: authProvider.isLoading
                      ? const Center(child: CircularProgressIndicator(color: primaryColor))
                      : ElevatedButton(
                          onPressed: () {
                            _showConfirmationBottomSheet(context);
                          },
                          style: ElevatedButton.styleFrom(
                            backgroundColor: primaryColor,
                            foregroundColor: Colors.white,
                            elevation: 0,
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(25),
                            ),
                          ),
                          child: const Text(
                            'Update my profile',
                            style: TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                        ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }

  void _showConfirmationBottomSheet(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);
    
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (BuildContext context) {
        return Padding(
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Warning Icon
              Container(
                width: 60,
                height: 60,
                decoration: const BoxDecoration(
                  color: Color(0xFFFF5252),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.priority_high,
                  color: Colors.white,
                  size: 36,
                ),
              ),
              const SizedBox(height: 20),
              
              // Title
              const Text(
                'Confirm profile update',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
              const SizedBox(height: 12),
              
              // Description
              const Text(
                'Are you sure you want to proceed\nwith these changes?',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 14,
                  color: Colors.grey,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 24),
              
              // Buttons
              Row(
                children: [
                  // Cancel Button
                  Expanded(
                    child: SizedBox(
                      height: 48,
                      child: OutlinedButton(
                        onPressed: () {
                          Navigator.pop(context);
                        },
                        style: OutlinedButton.styleFrom(
                          side: const BorderSide(color: Colors.grey),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(24),
                          ),
                        ),
                        child: const Text(
                          'Cancel',
                          style: TextStyle(
                            color: Colors.grey,
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  
                  // Yes, Update Button
                  Expanded(
                    child: SizedBox(
                      height: 48,
                      child: ElevatedButton(
                        onPressed: () async {
                          Navigator.pop(context); // Close bottom sheet
                          
                          final authProvider = Provider.of<AuthProvider>(context, listen: false);
                          
                          final success = await authProvider.updateProfile(
                            name: _nameController.text.trim(),
                            email: _emailController.text.trim(),
                            phoneNumber: _phoneController.text.trim(),
                            imagePath: _imagePath,
                          );

                          if (mounted) {
                            if (success) {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text('Profile updated successfully!'),
                                  backgroundColor: primaryColor,
                                ),
                              );
                            } else {
                              ScaffoldMessenger.of(context).showSnackBar(
                                SnackBar(
                                  content: Text(authProvider.error ?? 'Failed to update profile'),
                                  backgroundColor: Colors.red,
                                ),
                              );
                            }
                          }
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: primaryColor,
                          foregroundColor: Colors.white,
                          elevation: 0,
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(24),
                          ),
                        ),
                        child: const Text(
                          'Yes, Update',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
            ],
          ),
        );
      },
    );
  }

  @override
  void dispose() {
    _nameController.dispose();
    _phoneController.dispose();
    _emailController.dispose();
    super.dispose();
  }
}

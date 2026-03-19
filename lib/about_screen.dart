import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'widgets/user_avatar.dart';

class AboutScreen extends StatelessWidget {
  const AboutScreen({super.key});

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
          'About the app',
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
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Introduction
            Text(
              'Welcome to the Palasbari app, your comprehensive guide to the Garigaon Panchayat of Palasbari. Whether you are a resident or a visitor, our app is designed to provide you with all the information you need about local committees, government schemes, educational institutions, religious places, community clubs, and more.',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
                height: 1.6,
              ),
            ),

            const SizedBox(height: 24),

            // Key Features Title
            const Text(
              'Key Features:',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
            ),
            const SizedBox(height: 12),

            // Features List
            _buildBulletPoint(
              'Detailed information on district committees and their initiatives',
            ),
            _buildBulletPoint(
              'Updates on various government schemes and their beneficiaries',
            ),
            _buildBulletPoint(
              'Comprehensive guides to religious places like mandirs and namghars',
            ),
            _buildBulletPoint(
              'Information on local schools, colleges, and educational facilities',
            ),
            _buildBulletPoint(
              'Insights into local clubs, recreational activities, and community centers',
            ),
            _buildBulletPoint(
              'Detailed profiles of villages and their unique characteristics',
            ),

            const SizedBox(height: 24),

            // Mission Statement
            Text(
              'Our mission is to keep you informed and connected with everything happening in Palasbari, making it easier for you to engage with and contribute to our community.',
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
                height: 1.6,
              ),
            ),

            const SizedBox(height: 32),

            // App Version Title
            const Text(
              'App Version:',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.bold,
                color: Colors.black87,
              ),
            ),
            const SizedBox(height: 12),

            // Version Details
            _buildBulletPoint('Current Version: 1.0.0'),
            _buildBulletPoint('Release Date: August 6, 2024'),

            const SizedBox(height: 40),
          ],
        ),
      ),
    );
  }

  Widget _buildBulletPoint(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            '• ',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey.shade700,
              height: 1.6,
            ),
          ),
          Expanded(
            child: Text(
              text,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
                height: 1.6,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

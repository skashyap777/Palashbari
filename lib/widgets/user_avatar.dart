import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

class UserAvatar extends StatelessWidget {
  final double radius;
  final Color? backgroundColor;
  final Color? iconColor;

  const UserAvatar({
    super.key,
    this.radius = 18,
    this.backgroundColor,
    this.iconColor = Colors.white,
  });

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);
    
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        final photoUrl = authProvider.userProfile?.photoUrl;
        
        if (photoUrl != null && photoUrl.isNotEmpty) {
          return CircleAvatar(
            radius: radius,
            backgroundColor: Colors.transparent,
            backgroundImage: NetworkImage(photoUrl),
          );
        }
        
        return CircleAvatar(
          radius: radius,
          backgroundColor: backgroundColor ?? primaryColor,
          child: Padding(
            padding: EdgeInsets.all(radius * 0.4),
            child: Image.asset(
              'assets/icons/user_svgrepo_com.png',
              color: iconColor,
              fit: BoxFit.contain,
            ),
          ),
        );
      },
    );
  }
}

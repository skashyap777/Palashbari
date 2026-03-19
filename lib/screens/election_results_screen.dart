import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../widgets/user_avatar.dart';
import '../models/election_result.dart';
import '../services/api_service.dart';
import '../services/api_constants.dart';
import 'election_result_details_screen.dart';

class ElectionResultsScreen extends StatefulWidget {
  const ElectionResultsScreen({super.key});

  @override
  State<ElectionResultsScreen> createState() => _ElectionResultsScreenState();
}

class _ElectionResultsScreenState extends State<ElectionResultsScreen> {
  final List<ElectionYear> _years = [
    ElectionYear(type: 'Lok Sabha', year: 2001),
    ElectionYear(type: 'Lok Sabha', year: 2006),
    ElectionYear(type: 'Lok Sabha', year: 2011),
    ElectionYear(type: 'Assembly', year: 2003),
    ElectionYear(type: 'Assembly', year: 2008),
  ];

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

    // Grouping years by type
    Map<String, List<ElectionYear>> groupedYears = {};
    for (var year in _years) {
      groupedYears.putIfAbsent(year.type, () => []).add(year);
    }

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
        title: const Text(
          'Election Results',
          style: TextStyle(
            color: Colors.black87,
            fontSize: 20,
            fontWeight: FontWeight.bold,
          ),
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12.0),
            child: const UserAvatar(radius: 20),
          ),
        ],
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: groupedYears.length,
        itemBuilder: (context, index) {
          String type = groupedYears.keys.elementAt(index);
          List<ElectionYear> yearsList = groupedYears[type]!;

          return Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 12.0, horizontal: 8.0),
                child: Text(
                  type,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                    color: Colors.black54,
                  ),
                ),
              ),
              Card(
                elevation: 2,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                child: Column(
                  children: yearsList.map((year) {
                    return Column(
                      children: [
                        ListTile(
                          title: Text(
                            '${year.year}',
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.w500,
                            ),
                          ),
                          trailing: const Icon(Icons.chevron_right, color: primaryColor),
                          onTap: () {
                            Navigator.push(
                              context,
                              MaterialPageRoute(
                                builder: (context) => ElectionResultDetailsScreen(
                                  type: year.type,
                                  year: year.year.toString(),
                                ),
                              ),
                            );
                          },
                        ),
                        if (yearsList.indexOf(year) < yearsList.length - 1)
                          const Divider(height: 1, indent: 16, endIndent: 16),
                      ],
                    );
                  }).toList(),
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}

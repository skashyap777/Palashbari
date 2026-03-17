import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/election_result.dart';
import '../services/api_service.dart';
import '../services/api_constants.dart';

class ElectionResultDetailsScreen extends StatefulWidget {
  final String type;
  final String year;

  const ElectionResultDetailsScreen({
    super.key,
    required this.type,
    required this.year,
  });

  @override
  State<ElectionResultDetailsScreen> createState() => _ElectionResultDetailsScreenState();
}

class _ElectionResultDetailsScreenState extends State<ElectionResultDetailsScreen> {
  final ApiService _apiService = ApiService();
  
  List<ElectionResult> _results = [];
  bool _isLoading = false;
  String? _error;
  String? _selectedBooth;
  List<String> _booths = []; // Will be populated from API or results

  @override
  void initState() {
    super.initState();
    _loadResults();
  }

  Future<void> _loadResults() async {
    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      // In a real app, we'd fetch from API
      // For now, mirroring Android's example data if API fails or for demo
      final params = {
        'type': widget.type,
        'year': widget.year,
      };

      if (_selectedBooth != null) {
        params['booth'] = _selectedBooth!;
      }

      final response = await _apiService.fetchPaginatedData(
        endpoint: ApiConstants.electionResult,
        additionalParams: params,
      );

      final List<dynamic> data = response['data'] ?? [];
      
      setState(() {
        _results = data.map((json) => ElectionResult.fromJson(json)).toList();
        
        // Populate booths list if first load
        if (_booths.isEmpty && _results.isNotEmpty) {
          _booths = _results.map((r) => r.booth).toSet().toList();
          _booths.sort();
        }
        
        _isLoading = false;
      });
      
      // If API returns empty, use mock data as in Android project for demonstration
      if (_results.isEmpty) {
        _useMockData();
      }

    } catch (e) {
      print('Error loading election results: $e');
      _useMockData();
    }
  }

  void _useMockData() {
    setState(() {
      _results = [
        ElectionResult(
          booth: "Amtola Kaibartapara Bortola L.P. School (L/W)",
          pdfUrl: "https://www.adobe.com/support/products/enterprise/knowledgecenter/media/c4611_sample_explain.pdf",
        ),
        ElectionResult(
          booth: "Amtola Kaibartapara Bortola L.P. School (R/W)",
          pdfUrl: "https://morth.nic.in/sites/default/files/dd12-13_0.pdf",
        ),
      ];
      _booths = _results.map((r) => r.booth).toSet().toList();
      _isLoading = false;
    });
  }

  Future<void> _openPdf(String url) async {
    final uri = Uri.parse(url);
    if (await canalLaunch(uri)) {
      await launchUrl(uri, mode: LaunchMode.externalApplication);
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Could not open PDF')),
        );
      }
    }
  }

  // Placeholder for canalLaunch since it's a typo of canLaunchUrl
  Future<bool> canalLaunch(Uri uri) async {
    return await canLaunchUrl(uri);
  }

  @override
  Widget build(BuildContext context) {
    const primaryColor = Color(0xFF00BBA7);

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
          IconButton(
            icon: const Icon(Icons.filter_list, color: primaryColor),
            onPressed: _showBoothFilter,
          ),
        ],
      ),
      body: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Padding(
            padding: const EdgeInsets.all(16.0),
            child: Text(
              '${widget.type} / ${widget.year}',
              style: const TextStyle(
                fontSize: 14,
                color: primaryColor,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          Expanded(
            child: _isLoading
                ? const Center(child: CircularProgressIndicator(color: primaryColor))
                : _results.isEmpty
                    ? const Center(child: Text('No results found'))
                    : ListView.builder(
                        padding: const EdgeInsets.all(16),
                        itemCount: _results.length,
                        itemBuilder: (context, index) {
                          final result = _results[index];
                          return _buildResultCard(result);
                        },
                      ),
          ),
        ],
      ),
    );
  }

  Widget _buildResultCard(ElectionResult result) {
    const primaryColor = Color(0xFF00BBA7);
    
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 8.0, horizontal: 4.0),
          child: Text(
            result.booth,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.bold,
              color: Colors.black87,
            ),
          ),
        ),
        Card(
          elevation: 2,
          margin: const EdgeInsets.only(bottom: 24),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          child: InkWell(
            onTap: () => _openPdf(result.pdfUrl),
            borderRadius: BorderRadius.circular(16),
            child: Container(
              height: 150,
              width: double.infinity,
              decoration: BoxDecoration(
                color: Colors.grey.shade100,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.picture_as_pdf, color: Colors.red, size: 48),
                  const SizedBox(height: 12),
                  Text(
                    'View Document',
                    style: TextStyle(
                      color: Colors.grey.shade700,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ],
              ),
            ),
          ),
        ),
      ],
    );
  }

  void _showBoothFilter() {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.white,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return Container(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'Filter by Booth',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              if (_booths.isEmpty)
                const Text('No booths available')
              else
                Flexible(
                  child: ListView.builder(
                    shrinkWrap: true,
                    itemCount: _booths.length,
                    itemBuilder: (context, index) {
                      final booth = _booths[index];
                      return ListTile(
                        title: Text(booth),
                        trailing: _selectedBooth == booth
                            ? const Icon(Icons.check_circle, color: Color(0xFF00BBA7))
                            : null,
                        onTap: () {
                          setState(() {
                            _selectedBooth = booth;
                          });
                          Navigator.pop(context);
                          _loadResults();
                        },
                      );
                    },
                  ),
                ),
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: ElevatedButton(
                  onPressed: () {
                    setState(() {
                      _selectedBooth = null;
                    });
                    Navigator.pop(context);
                    _loadResults();
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.grey.shade200,
                    foregroundColor: Colors.black87,
                  ),
                  child: const Text('Clear Filter'),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}

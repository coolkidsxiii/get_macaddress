import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'device_admin_manager.dart';

void main() {
  runApp(const DeviceAdminApp());
}

class DeviceAdminApp extends StatelessWidget {
  const DeviceAdminApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Device Admin Info',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: DeviceInfoPage(),
    );
  }
}

class DeviceInfoPage extends StatefulWidget {
  @override
  _DeviceInfoPageState createState() => _DeviceInfoPageState();
}

class _DeviceInfoPageState extends State<DeviceInfoPage> {
  static const platform = MethodChannel('com.example.deviceadmin/info');

  String _serialNumber = 'กำลังตรวจสอบ...';
  String _macAddress = 'กำลังตรวจสอบ...';
  String _deviceAdminStatus = 'กำลังตรวจสอบ...';
  bool _isDeviceAdminEnabled = false;

  @override
  void initState() {
    super.initState();
    _requestDeviceAdminPermission();
  }

  Future<void> _requestDeviceAdminPermission() async {
    bool isGranted = await DeviceAdminManager.requestDeviceAdminPermission();
    setState(() {
      _isDeviceAdminEnabled = isGranted;
    });

    if (isGranted) {
      _fetchDeviceInfo();
    }
  }

  Future<void> _fetchDeviceInfo() async {
    try {
      final serialNumber = await platform.invokeMethod('getSerialNumber');
      final macAddress = await platform.invokeMethod('getMacAddress');
      final isDeviceAdmin = await platform.invokeMethod('isDeviceAdmin');

      setState(() {
        _serialNumber = serialNumber ?? 'ไม่พบ';
        _macAddress = macAddress ?? 'ไม่พบ';
        _deviceAdminStatus = isDeviceAdmin
            ? 'เปิดใช้งาน Device Admin'
            : 'ปิดใช้งาน Device Admin';
      });
    } on PlatformException catch (e) {
      setState(() {
        _serialNumber = 'เกิดข้อผิดพลาด: ${e.message}';
        _macAddress = 'เกิดข้อผิดพลาด: ${e.message}';
        _deviceAdminStatus = 'เกิดข้อผิดพลาด: ${e.message}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Device Admin Info'),
      ),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              _buildInfoCard('Serial Number', _serialNumber),
              SizedBox(height: 16),
              _buildInfoCard('MAC Address', _macAddress),
              SizedBox(height: 16),
              _buildInfoCard('Device Admin Status', _deviceAdminStatus),
              SizedBox(height: 16),
              ElevatedButton(
                onPressed: _isDeviceAdminEnabled
                    ? _fetchDeviceInfo
                    : _requestDeviceAdminPermission,
                child: Text(
                    _isDeviceAdminEnabled ? 'รีเฟรช' : 'อนุญาต Device Admin'),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildInfoCard(String title, String value) {
    return Card(
      elevation: 4,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(
              title,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            SizedBox(height: 8),
            Text(
              value,
              style: TextStyle(
                fontSize: 16,
                color: Colors.blue,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

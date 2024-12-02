import 'package:flutter/services.dart';

class DeviceAdminManager {
  static const MethodChannel _channel =
      MethodChannel('com.example.deviceadmin/device_admin');

  static Future<bool> requestDeviceAdminPermission() async {
    try {
      final bool result = await _channel.invokeMethod('requestDeviceAdmin');
      return result;
    } on PlatformException catch (e) {
      print('Error requesting device admin: ${e.message}');
      return false;
    }
  }
}

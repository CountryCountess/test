import javax.bluetooth.DeviceClass;
import javax.bluetooth.RemoteDevice;
import java.io.IOException;

/**
 * handles the Represenation of the available Bluetooth Devices
 * @see DeviceRepresentation
 */

public class DeviceRepresentation {

    RemoteDevice device;
    DeviceClass clazz;
    String deviceName;

    public DeviceRepresentation(RemoteDevice btDevice, DeviceClass cod) {
        this.clazz=cod;
        this.device = btDevice;
        try {
            String name = device.getFriendlyName(true);
            name += " (" + clazz + ")";
            deviceName = name;
        } catch (IOException e) {
            e.printStackTrace();
            deviceName = "Error";

        }

    }

    public RemoteDevice getDevice() {
        return device;
    }

    public DeviceClass getClazz() {
        return clazz;
    }

    @Override
    public String toString() {
        return deviceName;
    }
}

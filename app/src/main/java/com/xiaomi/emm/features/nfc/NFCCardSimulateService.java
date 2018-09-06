package com.xiaomi.emm.features.nfc;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;

/**
 * Created by Administrator on 2017/9/28.
 */

public class NFCCardSimulateService extends HostApduService {
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {

        return new byte[0];
    }

    @Override
    public void onDeactivated(int reason) {

    }
}

package com.xiaomi.emm.features.impl;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.xiaomi.emm.definition.Common;
import com.xiaomi.emm.definition.UrlConst;
import com.xiaomi.emm.features.db.DatabaseOperate;
import com.xiaomi.emm.features.http.RequestService;
import com.xiaomi.emm.features.resend.MessageResendManager;
import com.xiaomi.emm.model.TelephoyWhiteUser;
import com.xiaomi.emm.utils.DataParseUtil;
import com.xiaomi.emm.utils.MDM;
import com.xiaomi.emm.utils.PreferencesManager;
import com.xiaomi.emm.utils.TheTang;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TelephoneWhiteListImpl extends BaseImpl<RequestService> {
    private static final String TAG = "TelephoneWhiteListImpl";
    Context mContext;

    private static final String ACCOUNT_NAME_LOCAL_PHONE = "emm";
    private static final String ACCOUNT_TYPE_LOCAL_PHONE = "com.android.contacts.emm";
    private static final String GROUP_NAME = "EMM";
    long mGroupId = -1;

    public TelephoneWhiteListImpl(Context context) {
        super();
        this.mContext = context;
    }

    public void getTelephoneWhiteList() {
        final JSONObject telephoneObject = new JSONObject();
        try {
            telephoneObject.put("alias", PreferencesManager.getSingleInstance().getData(Common.alias));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody body = RequestBody.create(okhttp3.MediaType.parse(
                "application/json;charset=UTF-8"), telephoneObject.toString());
//        mService.getTelephoneWhiteList(body).enqueue(new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.PHONE_CONTACTS, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                final String content = TheTang.getSingleInstance().getResponseBodyString(response);

                if (!TheTang.getSingleInstance().whetherSendSuccess(content)) {
                    DatabaseOperate.getSingleInstance().add_backResult_sql(Common.get_telephone_white + "", telephoneObject.toString());
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<TelephoyWhiteUser> newList = DataParseUtil.jsonTelePhoneWhite(content);
                            excuteContacts(newList);
                        }
                    }).start();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                DatabaseOperate.getSingleInstance().add_backResult_sql(Common.get_telephone_white + "", telephoneObject.toString());
            }
        });
    }

    public void excuteContacts(List<TelephoyWhiteUser> newList) {
        DatabaseOperate.getSingleInstance().cleanTelephonyWhite();
        DatabaseOperate.getSingleInstance().addTelephonyWhiteList( newList );

        long id = getEmptyEmmContactGroups();
        if (id != -1) {
            insertContactToPhone(id, newList);
        }
        List<String> whiteNumbers = new ArrayList<>();
        for (TelephoyWhiteUser user:newList) {
            if (user.getTelephonyNumber() != null) {
                whiteNumbers.add(user.getTelephonyNumber());
//                Log.d("baii", "num " + user.getTelephonyNumber());
            }
            if (user.getShortPhoneNum() != null) {
                whiteNumbers.add(user.getShortPhoneNum());
//                Log.d("baii", "getShortPhoneNum " + user.getShortPhoneNum());
            }
        }
        MDM.mMDMController.setCallWhiteList(whiteNumbers);

       /* List<TelephoyWhiteUser> oldList = DatabaseOperate.getSingleInstance().queryTelephonyWhite();

        List<TelephoyWhiteUser> deleteList = new ArrayList<>();
        List<TelephoyWhiteUser> updateList = new ArrayList<>();
        List<TelephoyWhiteUser> addList = new ArrayList<>();

        //查找已经删除的
        for (int i = 0; i < oldList.size(); i++) {
            //表示删除
            boolean delete = true;
            for (int j = 0; j < newList.size(); j++) {
                if (oldList.get(i).getUserId().equals(newList.get(j).getUserId())) {
                    delete = false;
                }
            }

            if (delete) {
                deleteList.add(oldList.get(i));
            }

        }

        //查找新添加的
        for (int i = 0; i < newList.size(); i++) {

            //表示新添加
            boolean add = true;
            for (int j = 0; j < oldList.size(); j++) {

                if (oldList.get(j).getUserId().equals(newList.get(i).getUserId())) {
                    add = false;
                }
            }

            if (add) {
                addList.add(newList.get(i));
            }

        }

        //更新
        for (int i = 0; i < oldList.size(); i++) {

            for (int j = 0; j < newList.size(); j++) {

                if (oldList.get(i).getUserId().equals(newList.get(j).getUserId())) {

                    if (oldList.get(i).getUserName().equals(newList.get(j).getUserName())) {

                        if ( !oldList.get(i).getTelephonyNumber().equals(newList.get(j).getTelephonyNumber()) ) {

                            updateList.add(newList.get(j));
                            DatabaseOperate.getSingleInstance().addTelephonyWhiteList( updateList );
                            MDM.mMDMController.updateContact(oldList.get(i).getUserName(), oldList.get(i).getTelephonyNumber(),
                                    newList.get(j).getTelephonyNumber());

                        }

                    } else {
                        deleteList.add(oldList.get(i));
                        addList.add(newList.get(j));

                    }
                }
            }
        }

        MDM.deleteTelephonyWhiteList(deleteList, null);
        MDM.addTelephonyWhiteList(addList);*/

    }

    /**
     * 重发
     *
     * @param listener
     * @param body
     */
    public void reSendGetTelephoneWhiteList(final MessageResendManager.ResendListener listener, RequestBody body) {

//        mService.getTelephoneWhiteList(body).enqueue(new Callback<ResponseBody>() {
        mService.uploadInfo(UrlConst.PHONE_CONTACTS, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                String content = TheTang.getSingleInstance().getResponseBodyString(response);

                if (!TheTang.getSingleInstance().whetherSendSuccess(content)) {
                    listener.resendSuccess();
                } else {
                    listener.resendError();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                listener.resendFail();
            }
        });
    }

    private long getEmptyEmmContactGroups() {
        Uri uri = ContactsContract.Groups.CONTENT_URI;
        StringBuilder selection = new StringBuilder();
        selection.append(ContactsContract.Groups.DELETED + "=0");
        selection.append(" AND " + ContactsContract.Groups.TITLE + "=" + GROUP_NAME);
        Cursor groupCursor = mContext.getContentResolver().query(uri,
                new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE},
                ContactsContract.Groups.TITLE + "=?", new String[]{GROUP_NAME}, null);
        try {
            if (groupCursor != null && groupCursor.getCount() > 0) {
                if (groupCursor.moveToFirst()) {
                    long id = groupCursor.getLong(groupCursor.getColumnIndex(ContactsContract.Groups._ID));
                    deleteEmmContact(id);
                    return id;
                }
            } else {
                final ArrayList operationList = new ArrayList();
                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
                ContentValues groupValues = new ContentValues();

                groupValues.put(ContactsContract.Groups.ACCOUNT_NAME, ACCOUNT_NAME_LOCAL_PHONE);
                groupValues.put(ContactsContract.Groups.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL_PHONE);
                groupValues.put(ContactsContract.Groups.TITLE, GROUP_NAME);
                groupValues.put(ContactsContract.Groups.GROUP_IS_READ_ONLY, 1);

                builder.withValues(groupValues);
                operationList.add(builder.build());
                try {
                    ContentProviderResult[] results = mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
                    long id = ContentUris.parseId(results[0].uri);
                    groupValues.clear();
                    operationList.clear();
                    return id;
                } catch (RemoteException e) {
                    e.printStackTrace();
                } catch (OperationApplicationException e) {
                    e.printStackTrace();
                }

            }
        } finally {
            if (groupCursor != null) {
                groupCursor.close();
            }
        }
        return -1;
    }

    private void insertContactToPhone(long grpId, List<TelephoyWhiteUser> userList) {
        if (userList == null || userList.size()<1) {
            return;
        }
        for (TelephoyWhiteUser user : userList) {
            final ArrayList operationList = new ArrayList();
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
            ContentValues contactvalues = new ContentValues();
            contactvalues.put(ContactsContract.RawContacts.ACCOUNT_NAME, ACCOUNT_NAME_LOCAL_PHONE);
            contactvalues.put(ContactsContract.RawContacts.ACCOUNT_TYPE, ACCOUNT_TYPE_LOCAL_PHONE);
            builder.withValues(contactvalues);
            builder.withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DISABLED);//todo bai ?
            operationList.add(builder.build());

            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, user.getTelephonyNumber());
            builder.withValue(ContactsContract.RawContacts.Data.IS_PRIMARY, 1);//todo bai ?
            operationList.add(builder.build());

            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, user.getShortPhoneNum());
            operationList.add(builder.build());

            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, user.getUserName());
            operationList.add(builder.build());


            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE);//MIME类型
            builder.withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, grpId);//群组
            operationList.add(builder.build());

            try {
                mContext.getContentResolver().applyBatch(ContactsContract.AUTHORITY, operationList);
            } catch (RemoteException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            } catch (OperationApplicationException e) {
                Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
            }
        }
    }

    private void deleteEmmContact(long groupId) {
        if (groupId == -1) {
            return;
        }
        Uri uri = ContactsContract.Data.CONTENT_URI;
        StringBuilder selection = new StringBuilder();
        selection.append(ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=" + groupId);

        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID},
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=?", new String[]{ String.valueOf(groupId)},null);

        while (cursor.moveToNext()) {
            long rawId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID));
            mContext.getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI,
                    ContactsContract.RawContacts.CONTACT_ID + "=?", new String[] {String.valueOf(rawId)});
//            Log.d(TAG, "delete " + rawId);
        }
        if (cursor != null) {
            cursor.close();
        }
    }
}

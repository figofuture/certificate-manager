package com.figo.certmanager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class CertManagerActivity extends Activity {
	/** Called when the activity is first created. */

	String LOG_TAG = CertManagerActivity.class.getSimpleName();
	PackageManager mPM = null;
	Button mExport = null;
	TextView mIndicator = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.main);

		mPM = this.getPackageManager();

		mExport = (Button) findViewById(R.id.export);
		mIndicator = (TextView) findViewById(R.id.indicator);

		mExport.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new ExportTask().execute(new Long(0));
			}
		});
	}

	class ExportTask extends AsyncTask<Long, Integer, Long> {

		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
			// Toast.makeText(getApplicationContext(), "start",
			// Toast.LENGTH_LONG).show();
			mIndicator.setText(R.string.export_begin);
			mExport.setEnabled(false);
		}

		@Override
		protected Long doInBackground(Long... params) {
			List<PackageInfo> list = mPM
					.getInstalledPackages(PackageManager.GET_SIGNATURES);
			for (PackageInfo pif : list) {
				Log.i(LOG_TAG, pif.packageName);
				Signature[] sigs = pif.signatures;
				int i = 0;
				if (sigs.length > 0) {
					for (Signature sig : sigs) {
						// File file = new File(this.getFilesDir(),
						// pif.packageName + i + ".cer");
						X509Certificate x509 = null;
						try {
							x509 = X509Certificate.getInstance(sig
									.toByteArray());
							Log.i(LOG_TAG,
									"getSigAlgName: " + x509.getSigAlgName());
							Log.i(LOG_TAG,
									"getSigAlgOID: " + x509.getSigAlgOID());
							Log.i(LOG_TAG, "getEncoded: "
									+ x509.getEncoded().toString());
						} catch (CertificateException e) {
							e.printStackTrace();
						}

						File file = new File(
								getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
								pif.packageName + "." + i + ".cer");
						i++;
						FileOutputStream fos = null;
						try {
							fos = new FileOutputStream(file);
							fos.write(sig.toByteArray());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								fos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

					}
				}
			}
			return null;
		}

		protected void onPostExecute(Long result) {
			setProgressBarIndeterminateVisibility(false);
			// Toast.makeText(getApplicationContext(),
			// getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
			// Toast.LENGTH_LONG).show();
			mIndicator.setText(getResources().getString(R.string.export_end)
					+ getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
							.getAbsolutePath());
			mExport.setEnabled(true);
		}
	}
}
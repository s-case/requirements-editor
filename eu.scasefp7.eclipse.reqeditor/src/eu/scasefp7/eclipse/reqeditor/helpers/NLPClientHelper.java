package eu.scasefp7.eclipse.reqeditor.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.auth0.jwt.JWTSigner;

import eu.scasefp7.eclipse.reqeditor.Activator;

/**
 * Helper functions for communicating with the NLP server.
 * 
 * @author themis
 */
public class NLPClientHelper {

	/**
	 * Makes a REST request with JSON body to the NLP server and returns the response in JSON format.
	 * 
	 * @param query the JSON query to be sent.
	 * @return the JSON response of the request.
	 */
	public static String makeRestRequest(String query) {
		return makeRestRequest(query, false);
	}

	/**
	 * Retrieves a parameter from the secure preferences store.
	 * 
	 * @param parameterName the name of the parameter.
	 * @param parameterDefaultValue the default value if the parameter is not found or if it does not have a value.
	 * @return the value of the parameter given.
	 * @throws StorageException if there is a problem with the way the parameter is stored.
	 */
	private static String getParameterFromSecureStore(String parameterName, String parameterDefaultValue)
			throws StorageException {
		ISecurePreferences securePreferencesService = SecurePreferencesFactory.getDefault()
				.node("eu.scasefp7.eclipse.core.ui");
		return securePreferencesService != null ? securePreferencesService.get(parameterName, parameterDefaultValue)
				: parameterDefaultValue;
	}

	/**
	 * Retrieves a parameter from the preferences store.
	 * 
	 * @param parameterName the name of the parameter.
	 * @param parameterDefaultValue the default value if the parameter is not found or if it does not have a value.
	 * @return the value of the parameter given.
	 */
	private static String getParameterFromPreferenceStore(String parameterName, String parameterDefaultValue) {
		IPreferencesService preferencesService = Platform.getPreferencesService();
		return preferencesService != null ? preferencesService.getString("eu.scasefp7.eclipse.core.ui", parameterName,
				parameterDefaultValue, null) : parameterDefaultValue;
	}

	/**
	 * Revceives the S-CASE token and secret and creates the authorization header.
	 * 
	 * @param SCASEToken the S-CASE token.
	 * @param SCASESecret the S-CASE secret.
	 * @return an authorization header as a string.
	 * @throws WrongCredentialsException if the credentials are not correct.
	 */
	private static String getSignedCredentials(String SCASEToken, String SCASESecret) throws WrongCredentialsException {
		JWTSigner signer = new JWTSigner(SCASESecret);
		HashMap<String, Object> claims = new HashMap<String, Object>();
		claims.put("token", SCASEToken);
		if (claims.get("token") == "") {
			throw new WrongCredentialsException();
		}
		String signature = signer.sign(claims);
		return "CT-AUTH " + SCASEToken + ":" + signature;
	}

	/**
	 * Makes a REST request with JSON body to the NLP server and returns the response in JSON format.
	 * 
	 * @param query the JSON query to be sent.
	 * @param useControlTower boolean denoting whether to use the control tower.
	 * @return the JSON response of the request.
	 */
	public static String makeRestRequest(String query, boolean useControlTower) {
		if (useControlTower) {
			try {
				String CTAddress = getParameterFromSecureStore("controlTowerServiceURI", "https://app.scasefp7.com/");
				String NLPServerAddress = CTAddress + "api/proxy/nlpserver/project";
				String SCASEToken = getParameterFromSecureStore("controlTowerServiceToken", "");
				String SCASESecret = getParameterFromSecureStore("controlTowerServiceSecret", "");
				return makeRestRequest(NLPServerAddress, SCASEToken, SCASESecret, query);
			} catch (WrongCredentialsException e) {
				showErrorMessage(Platform.getPreferencesService() != null);
				return null;
			} catch (StorageException e) {
				Activator.log("There is a problem with the secure storage", e);
				return null;
			}
		} else {
			String NLPServerRootAddress = getParameterFromPreferenceStore("nlpServiceURI",
					"http://nlp.scasefp7.eu:8080/");
			String NLPServerAddress = NLPServerRootAddress + "nlpserver/project";
			return makeRestRequest(NLPServerAddress, query);
		}
	}

	/**
	 * Makes a REST request with JSON body to the NLP server and returns the response in JSON format.
	 * 
	 * @param NLPServerAddress the address of the NLP server.
	 * @param query the JSON query to be sent.
	 * @return the JSON response of the request.
	 */
	public static String makeRestRequest(String NLPServerAddress, String query) {
		String response = null;
		try {
			URL url = new URL(NLPServerAddress);
			// Open POST connection
			URLConnection urlc = url.openConnection();
			urlc.setRequestProperty("Content-Type", "application/json");
			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);

			// Send query
			PrintStream ps = new PrintStream(urlc.getOutputStream(), false, "UTF-8");
			ps.print(query);
			ps.close();

			// Get result
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
			String l = null;
			while ((l = br.readLine()) != null) {
				response = l;
			}
			br.close();
		} catch (ConnectException e) {
			Activator.log("Error connecting to NLP server", e);
		} catch (IOException e) {
			Activator.log("Error reading response of NLP server", e);
		}
		return response;
	}

	/**
	 * Exception class used to denote that the provided credentials are wrong.
	 */
	@SuppressWarnings("serial")
	static class WrongCredentialsException extends Exception {
		public WrongCredentialsException() {
			super();
		}

		public WrongCredentialsException(String message) {
			super(message);
		}

		public WrongCredentialsException(String message, Throwable cause) {
			super(message, cause);
		}

		public WrongCredentialsException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Makes a REST request with JSON body to the NLP server and returns the response in JSON format.
	 * 
	 * @param NLPServerAddress the address of the NLP server.
	 * @param SCASEToken the S-CASE token.
	 * @param SCASESecret the S-CASE secret.
	 * @param query the JSON query to be sent.
	 * @return the JSON response of the request.
	 * @throws WrongCredentialsException when the credentials given are wrong.
	 */
	public static String makeRestRequest(String NLPServerAddress, String SCASEToken, String SCASESecret, String query)
			throws WrongCredentialsException {
		String response = null;
		try {
			URL url = new URL(NLPServerAddress);
			// Open POST connection
			HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
			urlc.setRequestProperty("Content-Type", "application/json");

			// Add authorization parameters
			String credentials = getSignedCredentials(SCASEToken, SCASESecret);
			urlc.setRequestProperty("AUTHORIZATION", credentials);

			urlc.setDoOutput(true);
			urlc.setAllowUserInteraction(false);

			// Send query
			PrintStream ps = new PrintStream(urlc.getOutputStream(), false, "UTF-8");
			ps.print(query);
			ps.close();

			// Get result
			if (urlc.getResponseCode() == 401) {
				throw new WrongCredentialsException();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
			String l = null;
			while ((l = br.readLine()) != null) {
				response = l;
			}
			br.close();
		} catch (ConnectException e) {
			Activator.log("Error connecting to NLP server", e);
		} catch (IOException e) {
			Activator.log("Error reading response of NLP server", e);
		}
		return response;
	}

	/**
	 * Show an error message to the user.
	 * 
	 * @param isEclipse boolean to select to open window in Eclipse ({@code true}) or standalone ({@code false}).
	 */
	private static void showErrorMessage(boolean isEclipse) {
		if (isEclipse) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
					MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR);
					dialog.setText("Authorization problem");
					dialog.setMessage("Please provide a valid S-CASE token and a valid S-CASE secret");
					dialog.open();
				}
			});
		} else {
			Shell shell = new Shell();
			MessageBox dialog = new MessageBox(shell, SWT.ICON_ERROR);
			dialog.setText("Authorization problem");
			dialog.setMessage("Please provide a valid S-CASE token and a valid S-CASE secret");
			dialog.open();
		}
	}
}

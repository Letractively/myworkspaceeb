
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringBufferInputStream;
import java.security.Key;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * 
 * @author loadrunner ��������Ӧ�õ�Ŀ����չʾһ���ԳƼ��ܺͽ��ܵĹ��̣���ν�Գƣ�����Ϊ���ܺͽ���ʱ���õ���Կ��һ���ġ� ���裺
 *         һ�����ɶԳ���Կ������д�뵽��Կ�ļ������ļ�����ʽ���Զ���� �����öԳ���Կ��һ���ַ������м��ܣ���չʾ���ܵĽ��
 *         �����öԳ���Կ��������ܵĽ�����н��ܣ���չʾ���ܵĽ������У���Ƿ���δ����ʱ���ַ���һ��
 */
public class AESTest {

	// ����Գ���Կ���ļ� ����ɸ��������Զ����
	private static final String keyFile = "c:\\symmetryKey.key";

	// ���ڱ����ܵ��ַ���
	private static final String password = "eagle8888helloword";

	// ������ܺ�ת������������
	private static byte[] encrytedBytes;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("-----------�ԳƼ��ܲ���-----------");
		System.out.println("����֮ǰ������:" + password);
		generateKey(); // ���ɶԳ���Կ�������Գ���Կд�뵽symmetryKey.key�ļ���ȥ���ļ����Զ����
		encrypt(); // ��eagle8888helloword�ַ������жԳƼ���
		decrypt(); // �Լ��ܹ������ݽ��н��ܣ�����ǰ���Ƿ�һ��

	}

	/**
	 * ���ɶԳ���Կ
	 * 
	 * @throws Exception
	 */
	public static void generateKey() throws Exception {
		// ������Կ�������������㷨����AES
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		// ���������������
		SecureRandom random = new SecureRandom();
		// ��ʼ����Կ������
		generator.init(random);
		// ���ɶԳ���Կ
		SecretKey key = generator.generateKey();

		// �����ɵĶԳ���Կд�뵽�ļ���ȥ
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				keyFile));
		out.writeObject(key);
		out.flush();
		out.close();
	}

	/**
	 * ���ַ�������
	 * 
	 * @throws Exception
	 */
	public static void encrypt() throws Exception {
		// ����Կ�ļ��ж�ȡ�Գ���Կ
		ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(
				keyFile));
		Key key = (Key) keyIn.readObject();
		keyIn.close();

		// ����������󣬲���AES�㷨
		Cipher cipher = Cipher.getInstance("AES");
		// ����ģʽΪ����ģʽ����ʾ�ö��������ڼ��ܵģ�key�ǶԳ���Կ���������������ݽ����ϼ��ܣ���������һ��
		cipher.init(Cipher.ENCRYPT_MODE, key);

		// ��Ҫ�����ܵ��ַ���ת����������
		StringBufferInputStream sin = new StringBufferInputStream(password);

		// �������巽����ʼ���ܣ������ؼ��ܺ������
		encrytedBytes = doProccess(sin, cipher);
		sin.close();

		// �����ǽ����ܺ���������ַ�������ʽչʾ
		String d = "";
		for (int i = 0; i < encrytedBytes.length; i++) {
			int v = encrytedBytes[i] & 0xff;
			if (v < 16)
				d += "0";
			d += Integer.toString(v, 16).toUpperCase() + " ";
		}
		System.out.println("���ܺ�������ǣ�" + d);
	}

	/**
	 * ���ܷ���
	 * 
	 * @throws Exception
	 */
	public static void decrypt() throws Exception {
		// ��ȡ�Գ���Կ
		ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(
				keyFile));
		Key key = (Key) keyIn.readObject();
		keyIn.close();

		// ����������󣬲���AES�㷨
		Cipher cipher = Cipher.getInstance("AES");

		// ����ģʽΪ����ģʽ����ʾ�ö��������ڽ��ܵģ�key�ǶԳ���Կ�����������Լ��ܹ������ݽ����Ͻ��ܣ�������һ��
		cipher.init(Cipher.DECRYPT_MODE, key);

		// �����ܹ�������תΪ�ֽ�����������
		ByteArrayInputStream bin = new ByteArrayInputStream(encrytedBytes);

		// �������巽������ʼ���ܣ������ؽ��ܺ������
		byte[] b = doProccess(bin, cipher);
		bin.close();

		// �����ܵ����ݽ��ת��Ϊ�ַ������ȶ�δ���ܽ����������ݣ����Ƿ���ͬ
		System.out.println("���ܳ�������Ϊ��" + new String(b));
	}

	/**
	 * ���ܺͽ��ܵ�������
	 * 
	 * @param in
	 * @param cipher
	 * @return
	 * @throws Exception
	 */
	public static byte[] doProccess(InputStream in, Cipher cipher)
			throws Exception {
		// ���ݿ�Ĵ�С�����ܻ���ܹ����У��������ݿ����ʽִ�У�ÿ�����ݿ���������ֽڡ�
		// blockSize���Ǳ�ʾÿ�����пɴ�ŵ��ֽ���
		int blockSize = cipher.getBlockSize();

		// �������ֽ�������ʾ���ܻ����һ������ʱ�����ص��ֽ��������ֵ
		int outSize = cipher.getOutputSize(blockSize);

		// ���һ�����ݵ��ֽ������飬����������飬��ʾһ������
		byte[] inBytes = new byte[blockSize];

		// ���һ�����ݼ��ܻ���ܺ���ֽ�����
		byte[] outBytes = new byte[outSize];

		int inLength = 0;
		boolean more = true;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (more) {
			// ���ض�ȡ���ֽ��������inBytes��������inLength����һ�����ݵĳ���
			inLength = in.read(inBytes);
			if (inLength == blockSize) // �����ȡ�����ֽ����ﵽһ�����ݵĳ���
			{
				// �Ը����ݿ���м��ܻ����
				// ������
				// ����ʱ�����������ݿ���ֽ����飬��ʼλ�ã���ȡ���ȣ����ת�����ݵ��ֽ�����
				int outLength = cipher.update(inBytes, 0, inLength, outBytes);
				// ��ת���������д�������
				bout.write(outBytes, 0, outLength);
			} else
				more = false;
		}
		// inLength>0��ʾ������while�����һ�ε�inLength=in.read(inBytes);���������ݲ���һ�����ݿ�
		// ��Ϊ����������е��ֽ����պ����������ݿ���ֽ����������һ��inLength=in.read(inBytes)��
		// inLengthӦ����-1
		// doFinal������ת�����һ�����ݣ�������ã�����û�����һ������
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();

		// �����һ��ת���������д�������
		bout.write(outBytes);
		// ��������е�����ת�����ֽ����鷵��
		byte[] b = bout.toByteArray();
		bout.close();
		return b;
	}

}

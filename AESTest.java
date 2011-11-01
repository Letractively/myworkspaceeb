
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
 * @author loadrunner 描述：该应用的目的是展示一个对称加密和解密的过程，所谓对称，是因为加密和解密时所用的密钥是一样的。 步骤：
 *         一、生成对称密钥，将其写入到密钥文件，该文件及格式是自定义的 二、用对称密钥对一个字符串进行加密，并展示加密的结果
 *         三、用对称密钥对上面加密的结果进行解密，并展示解密的结果，以校验是否与未加密时的字符串一样
 */
public class AESTest {

	// 存入对称密钥的文件 ，这可格都是自已自定义的
	private static final String keyFile = "c:\\symmetryKey.key";

	// 用于被加密的字符串
	private static final String password = "eagle8888helloword";

	// 保存加密后转换出来的数据
	private static byte[] encrytedBytes;

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("-----------对称加密测试-----------");
		System.out.println("加密之前的数据:" + password);
		generateKey(); // 生成对称密钥，并将对称密钥写入到symmetryKey.key文件中去，文件是自定义的
		encrypt(); // 对eagle8888helloword字符串进行对称加密
		decrypt(); // 对加密过的数据进行解密，看看前后是否一致

	}

	/**
	 * 生成对称密钥
	 * 
	 * @throws Exception
	 */
	public static void generateKey() throws Exception {
		// 创建密钥生成器，加密算法采用AES
		KeyGenerator generator = KeyGenerator.getInstance("AES");
		// 生成随机数机对象
		SecureRandom random = new SecureRandom();
		// 初始化密钥生成器
		generator.init(random);
		// 生成对称密钥
		SecretKey key = generator.generateKey();

		// 将生成的对称密钥写入到文件中去
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
				keyFile));
		out.writeObject(key);
		out.flush();
		out.close();
	}

	/**
	 * 对字符串加密
	 * 
	 * @throws Exception
	 */
	public static void encrypt() throws Exception {
		// 从密钥文件中读取对称密钥
		ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(
				keyFile));
		Key key = (Key) keyIn.readObject();
		keyIn.close();

		// 创建密码对象，采用AES算法
		Cipher cipher = Cipher.getInstance("AES");
		// 设置模式为加密模式，表示该对象是用于加密的，key是对称密钥，是用它来对数据进行上加密，就像上锁一样
		cipher.init(Cipher.ENCRYPT_MODE, key);

		// 将要被加密的字符串转换成输入流
		StringBufferInputStream sin = new StringBufferInputStream(password);

		// 调用主体方法开始加密，并返回加密后的数据
		encrytedBytes = doProccess(sin, cipher);
		sin.close();

		// 下面是将加密后的数据以字符串的形式展示
		String d = "";
		for (int i = 0; i < encrytedBytes.length; i++) {
			int v = encrytedBytes[i] & 0xff;
			if (v < 16)
				d += "0";
			d += Integer.toString(v, 16).toUpperCase() + " ";
		}
		System.out.println("加密后的数据是：" + d);
	}

	/**
	 * 解密方法
	 * 
	 * @throws Exception
	 */
	public static void decrypt() throws Exception {
		// 读取对称密钥
		ObjectInputStream keyIn = new ObjectInputStream(new FileInputStream(
				keyFile));
		Key key = (Key) keyIn.readObject();
		keyIn.close();

		// 创建密码对象，采用AES算法
		Cipher cipher = Cipher.getInstance("AES");

		// 设置模式为解密模式，表示该对象是用于解密的，key是对称密钥，是用它来对加密过的数据进行上解密，就像开锁一样
		cipher.init(Cipher.DECRYPT_MODE, key);

		// 将加密过的数据转为字节数组输入流
		ByteArrayInputStream bin = new ByteArrayInputStream(encrytedBytes);

		// 调用主体方法，开始解密，并返回解密后的数据
		byte[] b = doProccess(bin, cipher);
		bin.close();

		// 将解密的数据结果转换为字符串，比对未解密进的明文数据，看是否相同
		System.out.println("解密出的数据为：" + new String(b));
	}

	/**
	 * 加密和解密的主调法
	 * 
	 * @param in
	 * @param cipher
	 * @return
	 * @throws Exception
	 */
	public static byte[] doProccess(InputStream in, Cipher cipher)
			throws Exception {
		// 数据块的大小，加密或解密过程中，采用数据块的形式执行，每个数据块包含数个字节。
		// blockSize就是表示每个块中可存放的字节数
		int blockSize = cipher.getBlockSize();

		// 输出块的字节数，表示加密或解密一块数据时，返回的字节数的最大值
		int outSize = cipher.getOutputSize(blockSize);

		// 存放一块数据的字节数据组，存满这个数组，表示一块数据
		byte[] inBytes = new byte[blockSize];

		// 存放一块数据加密或解密后的字节数组
		byte[] outBytes = new byte[outSize];

		int inLength = 0;
		boolean more = true;
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		while (more) {
			// 返回读取的字节数，如果inBytes填满，则inLength就是一块数据的长度
			inLength = in.read(inBytes);
			if (inLength == blockSize) // 如果读取到的字节数达到一块数据的长度
			{
				// 对该数据块进行加密或解密
				// 参数：
				// 加密时：被加密数据块的字节数组，开始位置，读取长度，存放转换数据的字节数组
				int outLength = cipher.update(inBytes, 0, inLength, outBytes);
				// 将转换后的数据写入输出流
				bout.write(outBytes, 0, outLength);
			} else
				more = false;
		}
		// inLength>0表示，上面while中最后一次的inLength=in.read(inBytes);读到的数据不够一个数据块
		// 因为如果输入流中的字节数刚好能整除数据块的字节数，则最后一次inLength=in.read(inBytes)后，
		// inLength应该是-1
		// doFinal方法是转换最后一块数据，必须调用，就算没有最后一块数据
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();

		// 将最后一块转换后的数据写入输出流
		bout.write(outBytes);
		// 将输出流中的数据转换成字节数组返回
		byte[] b = bout.toByteArray();
		bout.close();
		return b;
	}

}

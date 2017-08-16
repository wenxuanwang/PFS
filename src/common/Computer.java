package common;


import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;
import computeVar.ComputeVar;

public class Computer {

	public static double computeVar2(ComputeVar computeVar, double dbSize,
			double iphsion, double sen, double frequency, double limit) {
		try {
			MWNumericArray para1 = new MWNumericArray(Double.valueOf(dbSize),
					MWClassID.DOUBLE);
			MWNumericArray para2 = new MWNumericArray(Double.valueOf(iphsion),
					MWClassID.DOUBLE);
			MWNumericArray para3 = new MWNumericArray(Double.valueOf(sen),
					MWClassID.DOUBLE);
			MWNumericArray para4 = new MWNumericArray(
					Double.valueOf(frequency), MWClassID.DOUBLE);
			MWNumericArray para5 = new MWNumericArray(Double.valueOf(limit),
					MWClassID.DOUBLE);

			String result = computeVar.computeVar2(1, para1, para2, para3,
					para4, para5)[0].toString();
			return Double.valueOf(result);
		} catch (MWException e) {
			e.printStackTrace();
		}
		return dbSize * frequency;
	}

	public static void main(String[] args) throws MWException {
		ComputeVar computeVar = new ComputeVar();
		System.out.println(computeVar2(computeVar, 989818, 0.1, 50, 0.5, 0.9));
		System.out.println("over~");
	}
}

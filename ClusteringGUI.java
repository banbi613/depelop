
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

public class ClusteringGUI extends Applet implements ActionListener {
	private static final long serialVersionUID = 1L;

	private int nGauss = 5; // ガウス分布の個数
	private double sd = 20.0; // 標準偏差
	private int nCls = 5; // 目標クラスタ数
	private int nData = 1000; // データ数
	private int scrSize = 720; // スクリーンサイズ
	private int nStart = 10; // K-meansのクラスタリング回数
	private double WCV = Double.POSITIVE_INFINITY; // クラスタ内分散和
	int iterations = Integer.MAX_VALUE; // K-meansのクラスタリングの反復回数
	private ArrayList<Point> center = new ArrayList<>(); // ガウス分布の中心座標
	private Point[] data; // 各データの座標
	static Random RANDOM = new Random(1L);

	private double rOuter = 0.35; // リング外径比
	private double rInner = 0.25; // リングの内径比
	private double rVshift = 0.1; // 垂直シフト比
	private double rHshift = 0.05; // 水平シフト比
	private int dOuter = (int) (scrSize * rOuter); // リング外径
	private int dInner = (int) (scrSize * rInner); // リング内径
	private int dVshift = (int) (scrSize * rVshift); // 垂直シフト
	private int dHshift = (int) (scrSize * rHshift); // 水平シフト
	private Point Center = new Point(scrSize / 2, scrSize / 2); // 画像中心

	double[][] intDist = new double[nData][nData]; // クラスタ間距離
	int[] clsLabel = new int[nData]; // 各点のクラスタラベル
	int[] countLabel = new int[nData]; // 各クラスタラベルの出現数
	int[][] clsResult = new int[2][nData]; // クラスタリング結果

	static int cLabel1, cLabel2; // 最近傍クラスタのラベル対
	static int pLabel1, pLabel2; // 最近傍クラスタの点対

	private TextField rOuterAndNGaussTF; // リング外径比orガウス分布数指定用テキストフィールド
	private TextField rInnerAndSDTF; // リング内径比or標準偏差指定用テキストフィールド
	private TextField rVshiftTF; // 垂直シフト比指定用テキストフィールド
	private TextField rHshiftTF; // 水平シフト比指定用テキストフィールド

	private TextField nClsTF; // クラスタ数指定用テキストフィールド
	private TextField nStartTF; // クラスタリング回数指定用テキストフィールド

	private Label outerAndNGauss = new Label("Outer ratio =", Label.RIGHT);
	private Label innerAndNGauss = new Label("Inner ratio =", Label.RIGHT);
	private Label vshift = new Label("Vshift ratio =", Label.RIGHT);
	private Label hshift = new Label("Hshift ratio =", Label.RIGHT);

	private Label start;
	private JComboBox<String> generate;
	private JComboBox<String> clustering;
	private Button startBtn1, startBtn2; // アニメーション開始ボタン
	private Label elapsedTime;
	private Label iters;
	private Label wcv;

	private int[] xkCenter; // クラスタセンタのx座標
	private int[] ykCenter; // クラスタセンタのy座標

	private boolean initializedFlag = false; // 諸変数初期化済み判定フラグ
	private boolean animationFlag1 = false; // データ生成開始判定フラグ
	private boolean animationFlag2 = false; // クラスタリング開始判定フラグ

	static Random generator = new Random(1L); // 乱数ジェネレータ

	private Color colors[] = { Color.black, Color.blue, Color.red, Color.green,
			Color.orange, Color.cyan, Color.magenta, Color.pink, Color.yellow,
			Color.darkGray }; // 色配列

	public void init() {
		setSize(800, scrSize);
		setBackground(Color.white);
		setLayout(new FlowLayout(FlowLayout.RIGHT));

		rOuterAndNGaussTF = new TextField();
		rOuterAndNGaussTF.setText(String.valueOf(rOuter));
		rOuterAndNGaussTF.addActionListener(this);

		rInnerAndSDTF = new TextField();
		rInnerAndSDTF.setText(String.valueOf(rInner));
		rInnerAndSDTF.addActionListener(this);

		rVshiftTF = new TextField();
		rVshiftTF.setText(String.valueOf(rVshift));
		rVshiftTF.addActionListener(this);

		rHshiftTF = new TextField();
		rHshiftTF.setText(String.valueOf(rHshift));
		rHshiftTF.addActionListener(this);

		nStartTF = new TextField();
		nStartTF.setText(String.valueOf((nStart)));
		nStartTF.addActionListener(this);
		nStartTF.setVisible(false);

		start = new Label("nStart = ", Label.RIGHT);
		start.setVisible(false);

		startBtn1 = new Button("gen Data");
		startBtn1.addActionListener(this);

		String[] clsMethod = { "Single Linkage", "Complete Linkage",
				"Group Average", "Multi Start KMeans" };
		clustering = new JComboBox<>(clsMethod);
		clustering.addActionListener(this);

		String[] genData = { "2DringSplit", "2DGaussMix" };
		generate = new JComboBox<>(genData);
		generate.addActionListener(this);

		nClsTF = new TextField();
		nClsTF.setText(String.valueOf(nCls));
		nClsTF.addActionListener(this);

		startBtn2 = new Button("go Clustering");
		startBtn2.addActionListener(this);

		elapsedTime = new Label(" ");
		iters = new Label(" ");
		wcv = new Label(" ");

		Panel mainPnl = new Panel();
		mainPnl.setLayout(new GridLayout(13, 1));
		Panel[] pnl = new Panel[13];
		for (int i = 0; i < pnl.length; i++) {
			pnl[i] = new Panel();
			pnl[i].setLayout(new GridLayout(1, 2));
		}
		pnl[0].add(generate);
		pnl[1].add(outerAndNGauss);
		pnl[1].add(rOuterAndNGaussTF);
		pnl[2].add(innerAndNGauss);
		pnl[2].add(rInnerAndSDTF);
		pnl[3].add(vshift);
		pnl[3].add(rVshiftTF);
		pnl[4].add(hshift);
		pnl[4].add(rHshiftTF);
		pnl[5].add(startBtn1);
		pnl[6].add(clustering);
		pnl[7].add(new Label("NCls =", Label.RIGHT));
		pnl[7].add(nClsTF);
		pnl[8].add(start);
		pnl[8].add(nStartTF);
		pnl[9].add(startBtn2);
		pnl[10].add(elapsedTime);
		pnl[11].add(iters);
		pnl[12].add(wcv);

		for (int i = 0; i < pnl.length; i++) {
			mainPnl.add(pnl[i]);
		}

		add(mainPnl);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startBtn1) {
			animationFlag1 = true;
		}
		if (e.getSource() == startBtn2) {
			animationFlag2 = true;
		}

		// generateコンボボックスを変更した時
		if (e.getSource() == generate) {
			if (generate.getSelectedItem().equals("2DringSplit")) {
				nGauss = Integer.parseInt(rOuterAndNGaussTF.getText());
				sd = Double.parseDouble(rInnerAndSDTF.getText());

				outerAndNGauss.setText("Outer ratio");
				rOuterAndNGaussTF.setText(String.valueOf(rOuter));
				innerAndNGauss.setText("Inner ratio");
				rInnerAndSDTF.setText(String.valueOf(rInner));
				vshift.setVisible(true);
				rVshiftTF.setVisible(true);
				hshift.setVisible(true);
				rHshiftTF.setVisible(true);
			} else {
				rOuter = Double.parseDouble(rOuterAndNGaussTF.getText());
				rInner = Double.parseDouble(rInnerAndSDTF.getText());

				outerAndNGauss.setText("nGauss");
				rOuterAndNGaussTF.setText(String.valueOf(nGauss));
				innerAndNGauss.setText("SD");
				rInnerAndSDTF.setText(String.valueOf(sd));
				vshift.setVisible(false);
				rVshiftTF.setVisible(false);
				hshift.setVisible(false);
				rHshiftTF.setVisible(false);
			}
		}

		// clusteringコンボボックスを変更した時
		if (e.getSource() == clustering) {
			if (clustering.getSelectedItem().equals("Multi Start KMeans")) {
				nStartTF.setVisible(true);
				start.setVisible(true);
			} else {
				nStartTF.setVisible(false);
				start.setVisible(false);
			}
		}

		if (generate.getSelectedItem().equals("2DringSplit")) {
			rOuter = Double.parseDouble(rOuterAndNGaussTF.getText());
			rInner = Double.parseDouble(rInnerAndSDTF.getText());
		} else {
			nGauss = Integer.parseInt(rOuterAndNGaussTF.getText());
			sd = Double.parseDouble(rInnerAndSDTF.getText());
		}
		nCls = Integer.parseInt(nClsTF.getText());
		nStart = Integer.parseInt(nStartTF.getText());

		if (e.getSource() == startBtn1 || e.getSource() == startBtn2) {
			repaint();
		}
	}

	public void paint(Graphics g) {
		if (!initializedFlag) {
			initVariable();
		}

		// 初期化
		elapsedTime.setText("");
		iters.setText("");
		wcv.setText("");

		// アニメーション開始
		if (initializedFlag && animationFlag1) {
			g.clearRect(0, 0, scrSize, scrSize);
			if (generate.getSelectedItem().equals("2DringSplit"))
				gen2DringSplit();
			else if (generate.getSelectedItem().equals("2DGaussMix")) {
				centerCoo();
				genGauss();
			}
			drawData(g);
		}

		// クラスタリング開始
		if (animationFlag2) {
			long startTime = java.lang.System.currentTimeMillis();

			// 初期化
			initLabel();

			// クラスタ間距離の初期化
			calcDist(data);

			// 目標クラスタ数になるまでの反復
			int pCls = nData;

			switch (String.valueOf(clustering.getSelectedItem())) {
			case "Single Linkage":
				while (pCls > nCls) {
					searchMinDist();
					SingleLinkage();

					pCls--;
				}
				break;
			case "Complete Linkage":
				while (pCls > nCls) {
					searchMinDist();
					CompleteLinkage();

					pCls--;
				}
				break;
			case "Group Average":
				while (pCls > nCls) {
					searchMinDist();
					GroupAverage();

					pCls--;
				}
				break;
			case "Multi Start KMeans":
				xkCenter = new int[nCls]; // クラスタセンタのx座標
				ykCenter = new int[nCls]; // クラスタセンタのy座標
				int[] clsLabelStorage = new int[nData]; // 各点のクラスタラベル
				int[] countLabelStorage = new int[nData]; // 各クラスタラベルの出現数
				for (int i = 0; i < nStart; i++) {
					int iters = kmean();
					// クラスタ内分散和の計算
					double wcv = calcWCV();
					if (wcv < WCV) {
						WCV = wcv;
						iterations = iters;
						for (int j = 0; j < nData; j++) {
							clsLabelStorage[j] = new Integer(clsLabel[j]);
							countLabelStorage[j] = new Integer(countLabel[j]);
						}
					}
				}
				iters.setText("Iterations = " + iterations);
				wcv.setText("WCV = " + WCV);
				break;
			}

			// 最終クラスタリング結果
			finalResult();

			// クラスタリング結果の描画
			drawClusteringResult(g);

			// クラスタリング終了
			long endTime = java.lang.System.currentTimeMillis();
			long executionTime = endTime - startTime;

			elapsedTime.setText("Elapsed time = " + executionTime + " ms");
		}

		initializedFlag = false;
		animationFlag1 = false;
		animationFlag2 = false;
	}

	/**
	 * 乱数を用いた初期クラスタセンタの選択
	 */
	public void initCenter() {
		int count = 0;
		boolean isFound = false;

		while (count < nCls) {
			Point p = new Point();
			p = data[generator.nextInt(nData)];

			isFound = false;
			for (int i = 0; i < count; i++) {
				if (xkCenter[i] == p.x && ykCenter[i] == p.y) {
					isFound = true;
					break;
				}
			}

			if (isFound == true)
				continue;

			xkCenter[count] = p.x;
			ykCenter[count] = p.y;
			count++;
		}
	}

	/**
	 * 最近傍クラスタセンタの探索とクラスタラベルの決定
	 */
	public void searchNearestCenter() {
		double dMin, dist;
		int Label = 0;
		Point p = new Point();
		for (int j = 0; j < nData; j++) {
			p = data[j];
			dMin = Double.MAX_VALUE;
			for (int i = 0; i < nCls; i++) {
				Point q = new Point(xkCenter[i], ykCenter[i]);
				dist = calcPointDist2(p, q);
				if (dist < dMin) {
					dMin = dist;
					Label = i;
				}
			}
			clsLabel[j] = Label;
		}
	}

	/**
	 * クラスタセンタの更新と収束判定
	 * 
	 * @return 変化がなければtrue, あればfalse
	 */
	public boolean updateCenter() {
		double[] xMean = new double[nCls]; // 重心のx座標
		double[] yMean = new double[nCls]; // 重心のy座標
		int Label = 0;
		int change = 0;
		boolean isConvergent = false;
		Point p = new Point();

		// 初期化
		for (int i = 0; i < nCls; i++) {
			xMean[i] = 0.0;
			yMean[i] = 0.0;
			countLabel[i] = 0;
		}

		// クラスタ毎の点座標の加算
		for (int j = 0; j < nData; j++) {
			p = data[j];
			Label = clsLabel[j];
			xMean[Label] += p.x;
			yMean[Label] += p.y;
			countLabel[Label]++;
		}

		p = new Point();

		// 重心の計算と収束判定
		for (int i = 0; i < nCls; i++) {
			if (countLabel[i] == 0) { // 空クラスタの処理
				p = data[generator.nextInt(nData)];
			} else {
				p.x = (int) (xMean[i] / countLabel[i]);
				p.y = (int) (yMean[i] / countLabel[i]);
			}

			if (p.x != xkCenter[i] || p.y != ykCenter[i]) {
				change++;
			}
			xkCenter[i] = p.x;
			ykCenter[i] = p.y;
		}
		if (change == 0)
			isConvergent = true;
		return isConvergent;
	}

	/**
	 * 点間距離の計算
	 */
	public static double calcPointDist2(Point p, Point q) {
		return ((double) (p.x - q.x) * (p.x - q.x) + (double) (p.y - q.y)
				* (p.y - q.y));
	}

	/**
	 * : クラスタ内分散和の計算
	 * 
	 * @return WCVの計算結果
	 */
	public double calcWCV() {
		double var = 0.0;
		int Label;

		for (int j = 0; j < nData; j++) {
			Point p = new Point(0, 0);

			p = data[j];
			Label = clsLabel[j];
			Point q = new Point(xkCenter[Label], ykCenter[Label]);
			var += calcPointDist2(p, q);
		}
		return (var / (double) nData);
	}

	// データ群を描画するメソッド
	private void drawData(Graphics g) {
		int count = 0;
		for (Point p : data) {
			g.setColor(colors[clsLabel[count++]]);
			g.fillRect(p.x, p.y, 3, 3);
		}
	}

	// 諸変数を初期化するメソッド
	private void initVariable() {
		initializedFlag = (nGauss <= 10) ? true : false;
	}

	/**
	 * 中心点の座標を決める
	 */
	void centerCoo() {
		int x, y;
		center.clear();
		Point p;
		for (int i = 0; i < nGauss; i++) {
			while (true) {
				while (true) {
					x = 360 + (int) (RANDOM.nextGaussian() * 100);
					y = 360 + (int) (RANDOM.nextGaussian() * 100);
					if (sd <= x && x <= 720 - sd && sd <= y && y <= 720 - sd)
						break;
				}
				p = new Point(x, y);
				if (center.contains(p) == false) {
					center.add(p);
					break;
				}
			}
		}
	}

	/**
	 * ガウス分布の作成
	 */
	void genGauss() {
		data = new Point[nData];
		int x, y;
		int count = 0;
		int c = 0;
		Point p;
		Vector<Point> tempData = new Vector<Point>();
		try {
			count = 1000 / nGauss;
		} catch (ArithmeticException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
		}
		int result = 1000 - (count * nGauss);
		for (int i = 0; i < nGauss; i++) {
			if (i == nGauss - 1)
				count += result;
			for (int j = 0; j < count; j++) {
				while (true) {
					x = center.get(i).x + (int) (RANDOM.nextGaussian() * sd);
					y = center.get(i).y + (int) (RANDOM.nextGaussian() * sd);
					p = new Point(x, y);
					if (tempData.contains(p)) {
						continue;
					}
					clsLabel[c++] = 0;
					tempData.add(p);
					break;
				}
			}
		}
		data = (Point[]) tempData.toArray(new Point[0]);
	}

	// 一様乱数による分割された2次元環状分布を発生させるメソッド
	private void gen2DringSplit() {
		int x, y;
		int count = 0;
		Point datum;
		Vector<Point> tempData = new Vector<Point>();

		// 一様乱数による点データの発生

		while (tempData.size() < nData) {

			x = scrSize / 2 - dOuter + generator.nextInt(2 * dOuter);
			y = scrSize / 2 - dOuter + generator.nextInt(2 * dOuter);
			datum = new Point(x, y);
			double dist = calcPointDist(datum, Center);
			if ((int) dist >= dOuter || (int) dist <= dInner) {
				continue;
			}

			if (x > scrSize / 2) {
				x -= dHshift;
				y += dVshift;
			} else {
				x += dHshift;
				y -= dVshift;
			}
			datum = new Point(x, y);
			if (tempData.contains(datum)) {
				continue;
			}
			clsLabel[count++] = 0;
			tempData.add(datum);
		}

		data = (Point[]) tempData.toArray(new Point[0]);
	}

	// クラスタ番号の初期化
	public void initLabel() {
		for (int i = 0; i < nData; i++) {
			clsLabel[i] = i;
			countLabel[i] = 1;
		}
	}

	// 点間距離の計算
	public static double calcPointDist(Point p, Point q) {
		double dist;
		dist = (double) (p.x - q.x) * (p.x - q.x) + (double) (p.y - q.y)
				* (p.y - q.y);
		return Math.sqrt(dist);
	}

	// クラスタ間距離の初期化
	public void calcDist(Point[] data) {
		Point p, q;
		for (int j = 0; j < nData; j++) {
			q = data[j];
			intDist[j][j] = Double.MAX_VALUE; // 同一クラスタ内は∞
			for (int i = j + 1; i < nData; i++) {
				p = data[i];
				intDist[j][i] = calcPointDist(p, q);
				intDist[i][j] = intDist[j][i];
			}
		}
	}

	// 最小クラスタ間距離をもつクラスタ対の探索
	public void searchMinDist() {
		double dMin = Double.MAX_VALUE;
		int Label1, Label2;

		for (int j = 0; j < nData - 1; j++) {
			Label1 = clsLabel[j];

			for (int i = j + 1; i < nData; i++) {
				Label2 = clsLabel[i];

				if (intDist[j][i] < dMin) {
					dMin = intDist[j][i];
					cLabel1 = Label1;
					cLabel2 = Label2;
					pLabel1 = j;
					pLabel2 = i;
				}
			}
		}
	}

	// Group Average法によるクラスタ間距離およびクラスタラベルの更新
	public void GroupAverage() {
		double[] clstr_dsum = new double[nData]; // 融合クラスタとのラベル毎の点間距離和
		int[] clstr_pnts = new int[nData]; // 融合クラスタとのラベル毎の点対総数
		int Label1, Label2;

		// 融合クラスタのラベル更新
		for (int i = 0; i < nData; i++) {
			if (clsLabel[i] == cLabel2)
				clsLabel[i] = cLabel1;
		}

		// 融合クラスタ内の距離更新
		for (int j = 0; j < nData - 1; j++) {
			Label1 = clsLabel[j];
			if (Label1 != cLabel1)
				continue;
			for (int i = j + 1; i < nData; i++) {
				Label2 = clsLabel[i];
				if (Label2 == cLabel1) {
					intDist[j][i] = intDist[i][j] = Double.MAX_VALUE;
				}
			}
		}

		// 初期化
		for (int j = 0; j < nData; j++) {
			clstr_dsum[j] = 0.0;
			clstr_pnts[j] = 0;
		}

		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				clstr_dsum[clsLabel[i]] += intDist[j][i];
				clstr_pnts[clsLabel[i]]++;
			}
		}

		// 融合クラスタとのクラスタ間距離の変更
		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				if (clsLabel[i] != cLabel1) {
					intDist[j][i] = intDist[i][j] = clstr_dsum[clsLabel[i]]
							/ clstr_pnts[clsLabel[i]];
				}
			}
		}

		// クラスタラベルの出現数の更新
		for (int i = 0; i < nData; i++) {
			countLabel[i] = 0;
		}
		for (int i = 0; i < nData; i++) {
			countLabel[clsLabel[i]]++;
		}
	}

	// Single Linkage法によるクラスタ間距離およびクラスタラベルの更新
	public void SingleLinkage() {
		double[] clstr_dsum = new double[nData]; // 融合クラスタとのラベル毎の点間距離
		int Label1, Label2;

		// 融合クラスタのラベル更新
		for (int i = 0; i < nData; i++) {
			if (clsLabel[i] == cLabel2)
				clsLabel[i] = cLabel1;
		}

		// 融合クラスタ内の距離更新
		for (int j = 0; j < nData - 1; j++) {
			Label1 = clsLabel[j];
			if (Label1 != cLabel1)
				continue;
			for (int i = j + 1; i < nData; i++) {
				Label2 = clsLabel[i];
				if (Label2 == cLabel1) {
					intDist[j][i] = intDist[i][j] = Double.MAX_VALUE;
				}
			}
		}

		// 初期化
		for (int j = 0; j < nData; j++) {
			clstr_dsum[j] = Double.POSITIVE_INFINITY;
		}

		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				if (clstr_dsum[clsLabel[i]] > intDist[j][i])
					clstr_dsum[clsLabel[i]] = intDist[j][i];
			}
		}

		// 融合クラスタとのクラスタ間距離の変更
		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				if (clsLabel[i] != cLabel1) {
					intDist[j][i] = intDist[i][j] = clstr_dsum[clsLabel[i]];
				}
			}
		}

		// クラスタラベルの出現数の更新
		for (int i = 0; i < nData; i++) {
			countLabel[i] = 0;
		}
		for (int i = 0; i < nData; i++) {
			countLabel[clsLabel[i]]++;
		}
	}

	// Complete Linkage法によるクラスタ間距離およびクラスタラベルの更新
	public void CompleteLinkage() {
		double[] clstr_dsum = new double[nData]; // 融合クラスタとのラベル毎の点間距離
		int Label1, Label2;

		// 融合クラスタのラベル更新
		for (int i = 0; i < nData; i++) {
			if (clsLabel[i] == cLabel2)
				clsLabel[i] = cLabel1;
		}

		// 融合クラスタ内の距離更新
		for (int j = 0; j < nData - 1; j++) {
			Label1 = clsLabel[j];
			if (Label1 != cLabel1)
				continue;
			for (int i = j + 1; i < nData; i++) {
				Label2 = clsLabel[i];
				if (Label2 == cLabel1) {
					intDist[j][i] = intDist[i][j] = Double.MAX_VALUE;
				}
			}
		}

		// 初期化
		for (int j = 0; j < nData; j++) {
			clstr_dsum[j] = 0;
		}

		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				if (clstr_dsum[clsLabel[i]] < intDist[j][i]
						&& intDist[j][i] != Double.POSITIVE_INFINITY)
					clstr_dsum[clsLabel[i]] = intDist[j][i];
			}
		}

		// 融合クラスタとのクラスタ間距離の変更
		for (int j = 0; j < nData; j++) {
			if (clsLabel[j] != cLabel1)
				continue;
			for (int i = 0; i < nData; i++) {
				if (clsLabel[i] != cLabel1) {
					intDist[j][i] = intDist[i][j] = clstr_dsum[clsLabel[i]];
				}
			}
		}

		// クラスタラベルの出現数の更新
		for (int i = 0; i < nData; i++) {
			countLabel[i] = 0;
		}
		for (int i = 0; i < nData; i++) {
			countLabel[clsLabel[i]]++;
		}
	}

	// MultiStartKMeans法によるクラスタリング
	public int kmean() {
		boolean isConvergent = false;
		WCV = Double.POSITIVE_INFINITY;
		int iters = 0;
		initCenter();
		do {
			isConvergent = false;

			// 最近傍クラスタセンタの探索
			searchNearestCenter();
			// クラスタセンタの更新
			isConvergent = updateCenter();
			iters++;
		} while (isConvergent == false);

		return iters;
	}

	// 最終クラスタリング結果
	public void finalResult() {
		int count = 0;
		int Label;

		for (int i = 0; i < nData; i++) {
			if (countLabel[i] > 0) {
				clsResult[0][count] = i;
				clsResult[1][count] = countLabel[i];
				count++;
			}
		}

		// 各点の最終的クラスタラベル
		for (int j = 0; j < nData; j++) {
			Label = clsLabel[j];
			for (int k = 0; k < nCls; k++) {
				if (Label == clsResult[0][k]) {
					Label = k;
					break;
				}
			}
			clsLabel[j] = Label;
		}
	}

	// クラスタリング結果を描画するメソッド
	public void drawClusteringResult(Graphics g) {
		Point p;
		g.clearRect(0, 0, scrSize, scrSize);

		// クラスタリング結果を色分けして描画
		for (int j = 0; j < nData; j++) {
			p = data[j];
			g.setColor(colors[clsLabel[j]]);
			g.fillRect(p.x, p.y, 3, 3);
		}

		// 各クラスタのデータ数を色分けして描画
		for (int i = 0; i < nCls; i++) {
			g.setColor(colors[i]);
			g.drawString(i + 1 + "-th cluster: " + clsResult[1][i] + " points",
					645, 410 + 20 * i);
		}
	}
}
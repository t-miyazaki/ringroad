package ringroad;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * 放射環状道路の可視化クラス
 *
 */
public class FieldView extends JPanel {

	private JFrame frame;
	private Field field;

	private Car car;

	private int drawMode;

	/**
	 * コンストラクタ
	 * 描画ウィンドウを初期化して表示する
	 *
	 * @param size ウィンドウの一辺の長さ(ピクセル単位)
	 */
	public FieldView(int size) {
		// メインウィンドウを作成
		frame = new JFrame("Ringroad Simulator");
		// 閉じるボタンで終了
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// ウィンドウ位置のデフォルト化
		frame.setLocationByPlatform(true);
		// サイズ変更不可
		frame.setResizable(false);
		// サイズ設定
		frame.setSize(size, size+24);

		// 描画用パネルを追加
		frame.add(this);
		// ウィンドウを表示
		frame.setVisible(true);

	}

	/**
	 * 現在の状態を描画する
	 *
	 * @param field 描画対象のField
	 */
	public void draw(Field field) {
		this.field = field;
		drawMode = 0;
		repaint();
	}

	// repaint()から呼び出される
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		if (drawMode == 0) {
			if (field == null) return;

			// 描画の開始
			//

			// 背景初期化
			g.setColor(Color.CYAN);
			g.fillRect(0, 0, getWidth(), getHeight());

			g.setColor(Color.BLACK);

			drawThis(g);

		} else if (drawMode == 1) {
			drawCarRoute(g);
		}
	}

	/**
	 * Fieldの現在の状態を描画する
	 *
	 */
	void drawThis(Graphics g) {
		for (int x = 0; x < field.numX; x++) {
			for (int y = 0; y < field.numY; y++) {
				for (int isec = 0; isec < 4; isec++) {
					int stepMax = field.getIntersection(x, y).lengthAt(isec) + 1;
					for (int step = 0; step < stepMax; step++) {
						int[] pos = calcPosition(x, y, isec, step);
						Color color = field.getColor(x, y, isec, step);
						g.setColor(color);
						fillPoint(g, pos[0], pos[1]);
					}
				}
			}
		}
	}

	/**
	 * Field内の車の座標(x,y,isec,step)から描画ウィンドウ内の描画位置を求める
	 *
	 */
	int[] calcPosition(int x, int y, int isec, int step) {
		// 交差点位置yの高度での環状道路の1区間のサイト数
		int dx = (int) Math.round((field.rc + (field.dY * y)) * 2 * Math.PI / field.numX);
		// 同環状道路の全周のサイト数(ラウンドアバウトのサイト含む)
		int nAll = (dx + 2) * field.numX;

		// 最小半径
		int dxMin = (int) Math.round(field.rc * 2 * Math.PI / field.numX);
		int nMin = (dxMin + 2) * field.numX;
		double rmin = R * nMin / Math.PI;

		int n = 0;
		switch (isec) {
		case 0:
			n = (field.dY + 2) * y + 1;
			break;
		case 2:
			n = (field.dY + 2) * y;
			break;
		case 1:
			n = (field.dY + 2) * y - step;
			break;
		case 3:
			n = (field.dY + 2) * y + 1 + step;
			break;
		}
		// 半径
		double rad = rmin + 2 * R * n;

		// 偏角
		double theta = 0;
		int m = 0; // assert 0 <= m < nAll
		switch (isec) {
		case 0: // 環状道路外側
			m = (dx + 2) * x - step;
			if (m < 0) m += nAll;
			theta = 2 * Math.PI * m / nAll - Math.atan2(R, rad);
			break;
		case 2: // 環状道路内側
			m = (dx + 2) * x + step + 1;
			if (m >= nAll) m -= nAll;
			theta = 2 * Math.PI * m / nAll - Math.atan2(R, rad);
			break;
		case 1: // 放射道路上り側
			//m = (dxMin + 2) * x;
			theta = 2 * Math.PI * x / field.numX - Math.atan2(R, rad);
			break;
		case 3: // 放射道路下り側
			//m = (dxMin + 2) * x;
			// FIXME: 微妙に位置がずれているのをいつか直す
			theta = 2 * Math.PI * x / field.numX + Math.atan2(R, rad);
			break;
		}

		// 描画パネルの中心座標
		int cx = getWidth() / 2;
		int cy = getHeight() / 2;

		int px = (int) Math.round(rad * Math.cos(theta));
		int py = (int) Math.round(rad * Math.sin(theta));
		return new int[] {cx + px, cy - py};
	}


	/**
	 * 中心(x, y), 半径rの円を塗りつぶす
	 *
	 * @param g 描画対象のGraphics
	 * @param x 中心のx座標
	 * @param y 中心のy座標
	 * @param r 半径r
	 */
	void fillCircle(Graphics g, double x, double y, double r) {
		int xx = (int) Math.round(x - r);
		int yy = (int) Math.round(y - r);
		int rr = (int) Math.round(r * 2);
		g.fillOval(xx, yy, rr, rr);
	}

	/**
	 * 小さな固定サイズのドットを打ち込む
	 *
	 * @param g 描画対象のGraphics
	 * @param x 中心のx座標
	 * @param y 中心のy座標
	 */
	void fillDot(Graphics g, int x, int y) {
		g.fillRect(x, y, 1, 1);
		g.fillRect(x, y-1, 1, 1);
		g.fillRect(x, y+1, 1, 1);
		g.fillRect(x-1, y, 1, 1);
		g.fillRect(x+1, y, 1, 1);
	}

	/**
	 * プロット点を描画する（大きさによってメソッドを切り替える）
	 *
	 * @param g 描画対象のGraphics
	 * @param x 中心のx座標
	 * @param y 中心のy座標
	 */
	void fillPoint(Graphics g, int x, int y) {
		if (R < 2) {
			fillDot(g, x, y);
		} else {
			fillCircle(g, x, y, R*0.8);
		}
	}

	// テスト(デバッグ用)
	public void test(Graphics g, int numX, int numY, int rc) {
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, getWidth(), getHeight());

		// 描画パネルの中心座標
		int cx = getWidth() / 2;
		int cy = getHeight() / 2;

		g.setColor(Color.BLACK);

		/*
		 * 描画の座標系
		 *
		 *    ┼──→Ｘ
		 *    │
		 *    ↓
		 *    Ｙ
		 */

		int n = 100;

		double radius = R * n / Math.PI; // 必要半径

		// 環状道路の内側車線を描画
		for (int i = 0; i < n; i++) {
			double x = (cx + radius * Math.cos(2*Math.PI*i/n));
			double y = (cy + radius * Math.sin(2*Math.PI*i/n));
			//fillCircle(g, x, y, R);
			fillPoint(g, (int) x, (int) y);
		}
		radius += 2*R;
		// 環状道路の外側車線を描画
		for (int i = 0; i < n; i++) {
			double x = (cx + radius * Math.cos(2*Math.PI*i/n));
			double y = (cy + radius * Math.sin(2*Math.PI*i/n));
			//fillCircle(g, x, y, R);
			fillPoint(g, (int) x, (int) y);
		}

		numY = 8;
		int dY = 10;
		// 放射道路を描画
		double rad = radius + 2*R;
		for (int numy = 0; numy < numY; numy++) {
			for (int dy = 0; dy < dY; dy++) {
				double x = cx + (rad + dy*2*R) * Math.cos(2*Math.PI*numy/numY);
				double y = cy + (rad + dy*2*R) * Math.sin(2*Math.PI*numy/numY);

				double rx = R * Math.sin(2*Math.PI*numy/numY);
				double ry = R * Math.cos(2*Math.PI*numy/numY);

				fillPoint(g, (int) Math.round(x-rx), (int) Math.round(y+ry));
				fillPoint(g, (int) Math.round(x+rx), (int) Math.round(y-ry));
			}
		}
	}

	/**
	 *  車の経路情報を描画する(デバッグ用)
	 */
	public void drawCarRoute(Graphics g) {
		int[][] route = car.route;
		int i = 0;
		g.setColor(Color.BLUE);
		while (true) {
			if (route[i][0] == -1) break;
			int[] pos = calcPosition(route[i][0], route[i][1], route[i][2], 0);
			fillPoint(g, pos[0], pos[1]);
			i++;
		}
	}

	/**
	 * スレッドの実行をmsミリ秒停止する
	 *
	 * @param ms 停止する時間(ミリ秒単位)
	 */
	public static void wait(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

	// 上質なグラフィックスを得るには最低2.0以上にする。
	// コンパクトにしたければ、多少荒くてもよければ1.8程度に指定する。
	/**
	 * プロット点サイズ
	 */
	double R = 3; // プロット点サイズ

}

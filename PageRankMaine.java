import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import java.io.FileWriter;

class PageRankAlgorithm
{
	int[][] date;
	int dateCount;
	double[][] queue; //PageRankを求める行列を格納する二次元配列
	int verticesNumber; //頂点の数を格納するメモリ
	
	int[] outrinkTotalNumberPi; //Piのアウトリンク先の数(|Pi|)
	double Hij; //配列Hの各要素
	double initialPageRank; //PageRankアルゴリズムの初期化(pai^(0)T)
	double[] newPageRank; //PageRankアルゴリズムのランク(pai^(k+1)T)
	
	Map<String, Double> score; //PageRankのスコアを順に格納する配列
	String[] scoreKey; //Map scoreのキーを代入する配列
	
	String[] verticesName; //頂点の名前を格納するメモリ
	int[] verticesNameNumber; //各頂点に対応させた数字を格納するメモリ
	
	double[] eTVector; //e^Tベクトル
	double[] eVector; //eベクトル
	double[] aVector; //aベクトル
	
	double hyperlinkRatioA; //ハイパーリンクする比率
	
	double[] reservationPlacenewPageRank; //算出したPageRankの各値を一時保存する回避地
	double[] oldPageRank; //最新のランクの一つ前を保存するメモリ
	
	String choiceQueue; //スコアを計算する行列の種類を格納するメモリ
	
	public void register() //頂点の数に応じて変化するレジスタ
	{
		queue = new double[verticesNumber][verticesNumber];
		outrinkTotalNumberPi = new int[verticesNumber];
		newPageRank = new double[verticesNumber];
		
		score = new HashMap<>();
		scoreKey = new String[verticesNumber];
		
		verticesName = new String[verticesNumber];
		verticesNameNumber = new int [verticesNumber];
		
		eTVector = new double[verticesNumber]; //e^Tベクトル
		eVector = new double[verticesNumber]; //eベクトル
		
		aVector = new double[verticesNumber]; //aベクトル
		
		reservationPlacenewPageRank = new double[verticesNumber];
		oldPageRank = new double [verticesNumber];
	}
	
	public void resetRegister() //レジスタの初期化を行うメソッド
	{
		dateCount = 0;
		Hij = 0.0;
		initialPageRank = 0.0;
		hyperlinkRatioA = -1.00;
		choiceQueue = "選択されていません。";
	}
	public void setTextDateFile() //スライドのデータをテキストファイルから入力するためのメソッド
	{
		try
		{
			//File file = new File("./data/sampledata.txt"); //頂点が6個の場合
			File file = new File("./data/compound.txt"); //人体における代謝ネットワーク
				
			if(!file.exists())
			{
				System.out.print("ファイルが存在しません。");
			}
			
			FileInputStream inputFile = new FileInputStream(file);
			InputStreamReader stream  = new InputStreamReader(inputFile,"UTF-8");
			
			BufferedReader bufferedReader = new BufferedReader(stream);
			String data;
			String[] col;
			ArrayList<String> col2 = new ArrayList<>();//colをループ外でも使うための複製配列
			ArrayList<String> col2Vertices = new ArrayList<>(); //データの頂点を格納する配列
			ArrayList<String> col2OutRink = new ArrayList<>(); //データのアウトリンク先を格納する配列
			
			while((data = bufferedReader.readLine()) != null) //ファイルのデータをString型にして一つずつcolに格納する
			{
				byte[] b = data.getBytes();
				
				data = new String(b, "Shift-JIS");
				
				col = data.split(" ", 0);
				
				for(int i = 0; i < col.length; i++)
				{
					col2.add(col[i]);
					
					if(i % 2 == 0)
					{
						col2Vertices.add(col[i]);
					}
					else
					{
						col2OutRink.add(col[i]);
					}
				}
			}
			
			//頂点の数を求める
			String[] col2Array; //col2を固定長配列にしたもの
			col2Array = new String[col2.size()];
			
			for(int i = 0; i < col2Array.length; i++)
			{
				col2Array[i] = col2.get(i);
			}
			for(int i = 0; i < col2Array.length; i++)
			{
				for(int j = i + 1; j < col2Array.length; j++)
				{
					if(col2Array[i].equals(col2Array[j]))
					{
						col2Array[j] = "empty";
					}
				}
			}
			for(int i = 0; i < col2Array.length; i++)
			{
				if((col2Array[i] != "empty"))
				{
					verticesNumber++;
				}
			}
			
			this.register();
			
			//date[][]にデータの内容を格納する
			date = new int[col2Vertices.size()][2];
			
			//String型対応
			dateCount = col2Vertices.size();
			
			int verticesNameCount = 0;
			
			for(int i = 0; i < col2Array.length; i++)
			{
				if((col2Array[i] != "empty"))
				{
					String n = col2Array[i];
					verticesName[verticesNameCount] = n;
					
					verticesNameNumber[verticesNameCount] = verticesNameCount + 1;
					
					verticesNameCount++;
				}
			}
			for(int j = 0; j < verticesName.length; j++)
			{
				for(int i = 0; i < col2Vertices.size(); i++)
				{
					if(col2Vertices.get(i).equals(verticesName[j]))
					{
						date[i][0] = verticesNameNumber[j];
					}
					if(col2OutRink.get(i).equals(verticesName[j]))
					{
						date[i][1] = verticesNameNumber[j];
					}
				}
			}
			
			bufferedReader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setDate() //頂点の数に応じた配列を作成するメソッド
	{
		System.out.println("頂点の数を入力してください。");
		
		Scanner scanner = new Scanner(System.in);
		while(true)
		{
			String input = scanner.next();
			verticesNumber = Integer.parseInt(input);
			if(verticesNumber > 0)
			{
				date = new int[verticesNumber * verticesNumber - 1][2];
				this.register();
				this.assignmentDate();
				break;
			}
			else
			{
				System.out.println("入力が不正です。1以上の数を入力してください。\n※小数点以下の数は切り捨てられます。");
				
				continue;
			}
		}
		scanner.close();
	}
	
	public void assignmentDate() //データを入力するメソッド
	{
		Scanner scanner3 = new Scanner(System.in);
		
		loop1: for(int j = 0; j < verticesNumber * verticesNumber - 1; j++)
		{
			System.out.println("データを入力してください。\nアウトリンク元を入力してからアウトリンク先を入力してください。\n終了する場合Eを入力してください。");
			
			for(int i = 0; i < 2; i++)
			{
				String input3 = scanner3.next();
				if(input3.equals("E"))
				{
					break loop1;
				}
				else
				{
					date[j][i] = Integer.parseInt(input3);
				}
			}
			dateCount++;
		}
		scanner3.close();
	}
	
	public void showDate() //入力したデータを表示するメソッド
	{
		System.out.println("現在の入力データを表示したい場合Pを入力してください。\n表示しない場合Sを入力してください。");
		
		while(true)
		{
			Scanner scanner6 = new Scanner(System.in);
			String input6 = scanner6.next();
			
			if(input6.equals("P"))
			{
				System.out.println("現在の入力データは以下の通りです。");
				
				for(int j = 0; j < dateCount; j++)
				{
					for(int i = 0; i < 2; i++)
					{
						System.out.print(verticesName[date[j][i] - 1] + " ");
					}
					System.out.print("\n");
				}
				System.out.print("\n");
				
				break;
			}
			else if(input6.equals("S"))
			{
				System.out.print("\n");
				
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				
				continue;
			}
		}
	}
	
	public void countOutRink() //各アウトリンクの総数を求めるメソッド
	{
		for(int k = 0; k < verticesNumber; k++)
		{
			for(int j = 0; j < dateCount; j++)
			{
				if(date[j][0] == (k + 1))
				{
					outrinkTotalNumberPi[k]++;
				}
			}
		}
	}
	
	public void showCountOutRink()
	{
		System.out.println("各頂点からのアウトリンクの総数を表示したい場合はPを入力してください。\n表示しない場合はSを入力してください。");
		
		while(true)
		{
			Scanner scanner8 = new Scanner(System.in);
			String input8 = scanner8.next();
			
			if(input8.equals("P"))
			{
				
				System.out.println("各頂点からのアウトリンクの総数は以下の通りです。");
				for(int i = 0; i < verticesNumber; i++)
				{
					System.out.println("頂点" + verticesName[i] + "のアウトリンクは" + outrinkTotalNumberPi[i] + "個あります。");
				}
				System.out.print("\n");
				
				break;
			}
			else if(input8.equals("S"))
			{
				System.out.print("\n");
				
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				
				continue;
			}
		}
	}
	
	public void setQueue() //行列を生成するメソッド
	{
		for(int i = 0; i < verticesNumber; i++)
		{
			eTVector[i] = 1;
			eVector[i] = 1;
		}
		
		System.out.println("行列を生成します。\nH行列を生成する場合Hを、S行列を生成する場合Sを、G行列を生成する場合Gを入力してください。");
		
		while(true)
		{
			Scanner scanner10 = new Scanner(System.in);
			String input10 = scanner10.next();
			
			if(input10.equals("H"))
			{
				this.setQueueH();
				break;
			}
			else if(input10.equals("S"))
			{
				this.setQueueS();
				break;
			}
			else if(input10.equals("G"))
			{
				this.setQueueG();
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
		}
		System.out.print("\n");
	}
	
	public void setQueueH() //行列Hを設定するメソッド
	{
		for(int i = 0; i < verticesNumber; i++)
		{
			for(int j = 0; j < dateCount; j++)
			{
				if(date[j][0] == i + 1)
				{
					Hij = 1 / (double)outrinkTotalNumberPi[i];
					
					queue[date[j][0] - 1][date[j][1] - 1] = Hij;
				}
			}
		}
		choiceQueue = "H";
	}
	
	public void setQueueS() //行列Sを設定するメソッド
	{
		this.setQueueH();
		
		double[] initial = new double [verticesNumber]; //1/nを頂点の数だけ格納する配列
		
		for(int i = 0; i < verticesNumber; i++)
		{
			initial[i] = 1 / (double)verticesNumber;
		}
		for(int i = 0; i < verticesNumber; i++) //aベクトルを作成する配列
		{
			if(outrinkTotalNumberPi[i] == 0)
			{
				aVector[i] = 1;
			}
			else
			{
				aVector[i] = 0;
			}
		}
		for(int i = 0; i < verticesNumber; i++) //1/n * aVector*eTVectorを計算する
		{
			for(int j = 0; j < verticesNumber; j++)
			{
				queue[j][i] = initial[i] * aVector[j] * eVector[i] + queue[j][i];
			}
		}
		choiceQueue = "S";
	}
	
	public void setQueueG() //G行列を生成するメソッド
	{
		this.setQueueS();
		
		if(hyperlinkRatioA < 0.00)
		{
			System.out.println("ハイパーリンクに従う比率を入力してください。");
			
			while(true)
			{
				Scanner scanner8 = new Scanner(System.in);
				String input8 = scanner8.next();
				double hyperlinkRatioATemporary = Double.parseDouble(input8);
				
				if((hyperlinkRatioATemporary > 0.00)&&(hyperlinkRatioATemporary < 1.00))
				{
					hyperlinkRatioA = hyperlinkRatioATemporary;
					break;
				}
				else
				{
					System.out.println("入力が不正です。入力し直してください。");
					continue;
				}
			}
		}
		
		double[] initial = new double[verticesNumber]; //1/nを頂点の数だけ格納する配列
		double[] hyperlinkRatioAQueue = new double[verticesNumber]; //hyperlinkRatioAを頂点の数だけ格納する配列
		double[][] queueSHyperlinkRatioA = new double[verticesNumber][verticesNumber]; //S*HyperlinkRatioAを格納する配列
		
		for(int i = 0; i < verticesNumber; i++)
		{
			initial[i] = 1 / (double)verticesNumber;
			hyperlinkRatioAQueue[i] = hyperlinkRatioA;
		}
		for(int i = 0; i < verticesNumber; i++) //(1-hyperlinkRatioA)*1/n*eVector*eTVectorを計算する
		{
			for(int j = 0; j < verticesNumber; j++)
			{
				queue[j][i] = (1.00 - hyperlinkRatioAQueue[i]) * initial[i] * eTVector[j] * eVector[i]  + queue[j][i] * hyperlinkRatioAQueue[i];
			}
		}
		choiceQueue = "G";
	}
	
	public void showQueue() //行列を表示するメソッド
	{
		System.out.println("生成した" + choiceQueue + "行列を表示したい場合Pを入力してください。\n表示しない場合Sを入力してください。\n※Pを入力すると表示に時間がかかる場合があります");
		
		while(true)
		{
			Scanner scanner7 = new Scanner(System.in);
			String input7 = scanner7.next();
			
			if(input7.equals("P"))
			{
				for(int i = 0; i < verticesNumber; i++)
				{
					for(int j = 0; j < verticesNumber; j++)
					{
						System.out.print(queue[i][j] + "	");
					}
					System.out.print("\n");
				}
				System.out.print("\n");
			}
			else if(input7.equals("S"))
			{
				System.out.print("\n");
				
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
		}
	}
	
	public void calculationPageRank() //スコアを出す計算を行うメソッド
	{
		initialPageRank = 1 / (double)verticesNumber;
		
		for(int i = 0; i < verticesNumber; i++) //初期化した各スコアを代入
		{
			newPageRank[i] = initialPageRank;
		}
		System.out.println("行列" + choiceQueue + "の初期化したPageRankの各スコアを表示する場合はPを入力してください。\n表示しない場合はSを入力してください。");
			
		while(true)
		{
			Scanner scannerFirstScorePrint = new Scanner(System.in);
			String inputFirstScorePrint = scannerFirstScorePrint.next();
				
			if(inputFirstScorePrint.equals("P"))
			{
				System.out.println("初期化したPageRankの各スコアは以下の通りです。");
				showPageRank();
				
				break;
			}
			else if(inputFirstScorePrint.equals("S"))
			{
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
		}
		
		System.out.println("行列" + choiceQueue + "の各スコアの計算回数を制限します。\n繰り返しの回数を定義に従わせる場合はDを入力してください。\n繰り返しの回数をキーボードから入力する場合はKを入力してください。\n※Dを入力すると計算に時間がかかる場合があります。");
		
		while(true)
		{
			Scanner scanner4 = new Scanner(System.in);
			String input4 = scanner4.next();
			
			if(input4.equals("D")) //定義に従って繰り返す場合
			{
				for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
				{
					double reservationPlace = 0.0;
					reservationPlacenewPageRank[j] = 0.0;
					
					for(int i = 0; i < verticesNumber; i++)
					{
						reservationPlace += newPageRank[i] * queue[i][j];
					}
					reservationPlacenewPageRank[j] = reservationPlace;
					
				}
				for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
				{
					newPageRank[j] = reservationPlacenewPageRank[j];
				}
				for(int i = 0; i < verticesNumber; i++)
				{
					oldPageRank[i] = newPageRank[i];
				}
				while(true)
				{
					double d = 0.0;
					
					for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
					{
						double reservationPlace = 0.0;
						reservationPlacenewPageRank[j] = 0.0;
						
						for(int i = 0; i < verticesNumber; i++)
						{
							reservationPlace += newPageRank[i] * queue[i][j];
						}
						reservationPlacenewPageRank[j] = reservationPlace;
						
					}
					for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
					{
						newPageRank[j] = reservationPlacenewPageRank[j];
					}
					
					for(int i = 0; i < verticesNumber; i++) //前後の距離を求める
					{
						d += ((newPageRank[i] - oldPageRank[i]) * (newPageRank[i] - oldPageRank[i]));
					}
					d = Math.sqrt(d);
					
					if(d < 0.1 * 0.1 * 0.1 * 0.1 * 0.1 * 0.1 * 0.1 * 0.1)
					{
						break;
					}
					else
					{
						for(int i = 0; i < verticesNumber; i++)
						{
							oldPageRank[i] = newPageRank[i];
						}
					}
				}
				break;
			}
			else if(input4.equals("K"))//繰り返しの数をキーボードから打ち込む場合
			{
				int numberOfTimes = 0;
				
				while(true)
				{
					Scanner scannerRetunKeyboard = new Scanner(System.in);
					String inputRetunKeyboard = scannerRetunKeyboard.next();
					
					try
					{
						numberOfTimes = Integer.parseInt(inputRetunKeyboard);
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
						System.out.println("入力が不正です。入力し直してください。");
						continue;
					}
					break;
				}
				for(int k = 0; k < numberOfTimes; k++)
				{
					for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
					{
						double reservationPlace = 0.0;
						reservationPlacenewPageRank[j] = 0.0;
						
						for(int i = 0; i < verticesNumber; i++)
						{
							reservationPlace += newPageRank[i] * queue[i][j];
						}
						reservationPlacenewPageRank[j] = reservationPlace;
						
					}
					for(int j = 0; j < verticesNumber; j++) //各スコアを計算して代入
					{
						newPageRank[j] = reservationPlacenewPageRank[j];
					}
				}
				break;
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
		}
		System.out.println("\nPageRankの各スコアを表示する場合はPを入力してください。\n表示しない場合はSを入力してください。");
			
		while(true)
		{
			Scanner scannerScorePrint = new Scanner(System.in);
			String inputScorePrint = scannerScorePrint.next();
			
			if(inputScorePrint.equals("P"))
			{
				System.out.println("PageRankの各スコアは以下の通りです。");
				showPageRank();
				
				break;
			}
			else if(inputScorePrint.equals("S"))
			{
				break;
				}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
		}
		
		this.setPageRank();
	}
	public void setPageRank() //各頂点名とPageRankのスコアをMapに格納するメソッド
	{
		for(int i = 0; i < verticesNumber; i++)
		{
			scoreKey[i] = verticesName[i];
			double scoreValue = newPageRank[i];
			
			score.put(scoreKey[i], scoreValue);
		}
	}
	
	public void showPageRank()
	{
		for(int i = 0; i < verticesNumber; i++)
		{
			scoreKey[i] = verticesName[i];
			double scoreValue = newPageRank[i];
			
			score.put(scoreKey[i], scoreValue);
		}
		
		for(int i = 0; i < verticesNumber; i++)
		{
			System.out.println("頂点名" + scoreKey[i] +" スコア" + score.get(scoreKey[i]));
		}
		System.out.print("\n");
	}
	public void showRankingPageRank()
	{
		List<Entry<String, Double>> listScore = new ArrayList<Entry<String, Double>>(score.entrySet());
		
		Collections.sort(listScore, new Comparator<Entry<String, Double>>()
		{
			public int compare(Entry<String, Double>object1, Entry<String, Double>object2)
			{
				return object2.getValue().compareTo(object1.getValue());
			}
		});
		
		System.out.println("\nランキングを表示します。\n全て(1位から" + verticesNumber + "位まで)を表示したい場合Sを入力してください。\nキーボードから何位までかをを入力する場合Kを入力してください。");
		
		while(true)
		{
			int rankingCount = 0;
			
			Scanner scannerRanking = new Scanner(System.in);
			String inputRanking = scannerRanking.next();
			
			if(inputRanking.equals("S"))
			{
				for(Entry<String, Double> entry : listScore)
				{
					System.out.println((rankingCount + 1) + ". " + entry.getKey() + "	" + entry.getValue());
					
					rankingCount++;
				}
				
				try
				{
					FileWriter fw = new FileWriter("C:\\soft\\jcpad\\Project1AResultDocument\\PageRankRanking.txt");
					
					rankingCount=0;
					for(Entry<String, Double> entry : listScore)
					{
						fw.write((rankingCount + 1) + ". " + entry.getKey() + "	" + entry.getValue() + "\r\n");
						rankingCount++;
					}
					fw.close();
					
					System.out.println("PageRankのランキングをテキストファイル『PageRankRanking.txt』に記録しました。");
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
				}
				
				break;
			}
			else if(inputRanking.equals("K"))
			{
				int rankingNumber = 0;
				
				System.out.println("何位まで出力するかを入力してください。");
				
				while(true)
				{
					Scanner scannerRankingKeyboard = new Scanner(System.in);
					String inputRankingKeyboard = scannerRankingKeyboard.next();
					
					try
					{
						rankingNumber = Integer.parseInt(inputRankingKeyboard);
					}
					catch(NumberFormatException e)
					{
						e.printStackTrace();
						System.out.println("入力が不正です。入力し直してください。");
						continue;
					}
					if((rankingNumber <= 0 )||(rankingNumber > verticesNumber))
					{
						System.out.println("入力が不正です。入力し直してください。");
						continue;
					}
					
					System.out.println(rankingNumber + "位まで出力します。");
					
					for(Entry<String, Double> entry : listScore)
					{
						System.out.println((rankingCount + 1) + "." + entry.getKey() + "	" + entry.getValue());
						
						rankingCount++;
						
						if(rankingCount == rankingNumber)
						{
							break;
						}
					}
					try
					{
						FileWriter fw = new FileWriter("C:\\soft\\jcpad\\Project1AResultDocument\\PageRankRankingTop" + rankingNumber + ".txt");
						
						rankingCount=0;
						for(Entry<String, Double> entry : listScore)
						{
							fw.write((rankingCount + 1) + ". " + entry.getKey() + "	" + entry.getValue() + "\r\n");
							rankingCount++;
							
							if(rankingCount == rankingNumber)
							{
								break;
							}
						}
						fw.close();
						
						System.out.println("PageRankのランキングをテキストファイル『PageRankRankingTop" + rankingNumber + ".txt』に記録しました。");
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
					}
					break;
				}
			}
			else
			{
				System.out.println("入力が不正です。入力し直してください。");
				continue;
			}
			break;
		}
	}
	
	public void recognitionNameAndNumber()
	{
		for(int i = 0; i < verticesName.length; i++)
		{
			System.out.println(i + "番	頂点名" +verticesName[i] + "	対応する番号" + verticesNameNumber[i]);
		}
	}
}
class PageRankMaine
{
	public static void main(String[] args)
	{
		PageRankAlgorithm ex1 = new PageRankAlgorithm();
		ex1.resetRegister();
		
		//データの格納
		//ex1.setDate();
		ex1.setTextDateFile();
		
		//各頂点からのアウトリンク総数の算出
		ex1.countOutRink();
		
		//ex1.alignmentDate();
		
		//各頂点に対応する整数の確認
		//ex1.recognitionNameAndNumber();
		
		//現在の状況表示
		ex1.showDate();
		ex1.showCountOutRink();
		
		//行列の格納
		ex1.setQueue();
		
		//行列の表示
		ex1.showQueue();
		
		//ランクの算出、表示
		ex1.calculationPageRank();
		
		//ランキングの表示
		ex1.showRankingPageRank();
	}
}
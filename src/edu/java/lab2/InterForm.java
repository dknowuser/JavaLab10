package edu.java.lab2;
/** 
 * @author Марчук*/

//Подключение графических библиотек
import java.awt.BorderLayout;
import java.awt.FileDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;

import java.awt.event.*;
import java.lang.Math;
import java.util.HashMap;
import java.util.Properties;
import java.io.*;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class InterForm {
	
	//Объект для записи сообщений
	private static final Logger log = Logger.getLogger("ТекущийКласс.class");
	static String Log4JPropertyFile = "E:/Eclipse/Lab10/src/main/resources/log4j.properties";
	static Properties p = new Properties();
	
	//Мониторы
	private static Object mutex1 = new Object();
	private static Object mutex2 = new Object();
	
	//Были ли нажаты соответствующие кнопки
	private boolean SavePressed = false;
	private boolean ReportPressed = false;
	
	//Методы для работы с xml-файлами
	public void XMLOpen() {
		//Создание диалогового окна выбора файла для загрузки данных
		FileDialog open = new FileDialog(InterForm, "Загрузка данных", FileDialog.LOAD);
		open.setFile("*.xml");
		open.setVisible(true);
		
		//Проверка, выбрали ли файл
		String FileName = open.getDirectory() + open.getFile();
		if(FileName == null) 
		{
			log.fatal("Не выбран файл.");
			return;
		}
		
		//Очищаем таблицу
		int rows = Model.getRowCount();
		for(int i = 0; i < rows; i++)
			Model.removeRow(0);
		
		Document doc = null;
		
		//Считываем данные
		try
		{
			//Создание парсера документа
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			//Чтение документа из файла
			doc = dBuilder.parse(new File(FileName));
			
			//Нормализация документа
			doc.getDocumentElement().normalize();
		}
		
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			log.error("Ошибка при открытии xml-файла.", e);
			
			e.printStackTrace();
		} 
		
		catch (SAXException e) {
			// TODO Auto-generated catch block
			log.error("Ошибка при открытии xml-файла.", e);
			
			e.printStackTrace();
		} 
		
		catch (IOException e) {
			// TODO Auto-generated catch block
			log.error("Ошибка при открытии xml-файла.", e);
			
			e.printStackTrace();
		}
		
		NodeList nlGames = doc.getElementsByTagName("game");
		
		//Записываем данные в таблицу
		for(int temp = 0; temp < nlGames.getLength(); temp++) {
			Node element = nlGames.item(temp);
			NamedNodeMap attrs = element.getAttributes();
			String name = attrs.getNamedItem("name").getNodeValue();
			String year = attrs.getNamedItem("year").getNodeValue();
			String chart = attrs.getNamedItem("chart").getNodeValue();
			
			Model.addRow(new String[] {name, year, chart});			
		}
	}
	
	public void XMLProcess() {
		//Создание диалогового окна выбора файла для сохранения данных
		FileDialog open = new FileDialog(InterForm, "Сохранение данных", FileDialog.SAVE);
		open.setFile("*.xml");
		open.setVisible(true);
		
		//Проверка, выбрали ли файл
		String FileName = open.getDirectory() + open.getFile();
		if(FileName == null) 
		{
			log.fatal("Не выбран файл.");
			return;
		}
		
		DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = f.newDocumentBuilder();
		} catch (ParserConfigurationException e2) {
			// TODO Auto-generated catch block
			log.error("Ошибка при сохранении xml-файла.", e2);
			e2.printStackTrace();
		}				
		Document doc = null;
		try {
			doc = builder.parse(FileName);
		} catch (SAXException | IOException e2) {
			// TODO Auto-generated catch block
			log.error("Ошибка при сохранении xml-файла.", e2);
			e2.printStackTrace();
		}
		
		//Сохраняем данные
		Node gamelist = doc.getDocumentElement();
		
		for(int i = 0; i < Model.getRowCount(); i++) {
			Element game = doc.createElement("game");
			gamelist.appendChild(game);
			game.setAttribute("name", (String)Model.getValueAt(i, 0));
			game.setAttribute("year", (String)Model.getValueAt(i, 1));
			game.setAttribute("chart", (String)Model.getValueAt(i, 2));
		}
		
		try {
			Transformer trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.METHOD, "xml");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
			
			trans.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(FileName)));
		}
		
		catch(TransformerConfigurationException e1)	{ 
			log.error("Ошибка при сохранении xml-файла.", e1);
			e1.printStackTrace();
		} 
		
		catch (TransformerException e1) {
			// TODO Auto-generated catch block
			log.error("Ошибка при сохранении xml-файла.", e1);
			e1.printStackTrace();
		} 
		
		catch (IOException e1) {
			// TODO Auto-generated catch block
			log.error("Ошибка при сохранении xml-файла.", e1);
			e1.printStackTrace();
		}	
	}
	
	public void HTMLPDFReport() {
		try {
			//Создание диалогового окна выбора файла для сохранения данных
			FileDialog open = new FileDialog(InterForm, "Создание отчёта", FileDialog.LOAD);
			open.setFile("*.xml");
			open.setVisible(true);
			
			//Проверка, выбрали ли файл
			String FileName = open.getDirectory() + open.getFile();
			if(FileName == null) 
			{
				log.fatal("Не выбран файл.");
				return;
			}
			
			//Указание источника XML-данных					
			//Создание парсера документа
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			
			//Чтение документа из файла					
			JRXmlDataSource ds = new JRXmlDataSource(FileName, "/gamelist/game");
			
			//Создание отчёта на базе шаблона
			JasperReport jasperReport = JasperCompileManager.compileReport("./report/report2.jrxml");
			
			//Заполнение отчёта данными
			JasperPrint print = JasperFillManager.fillReport(jasperReport, new HashMap(), ds);
			JRExporter exporterpdf = new JRPdfExporter();
			JRExporter exporterhtml = new JRHtmlExporter();
			
			//Задание имени файла для выгрузки отчёта
			exporterpdf.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, "./report/autoreport2.pdf");
			exporterhtml.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, "./report/autoreport2.html");
			
			//Подключение данных к отчёту
			exporterpdf.setParameter(JRExporterParameter.JASPER_PRINT, print);
			exporterhtml.setParameter(JRExporterParameter.JASPER_PRINT, print);
			
			//Выгрузка отчёта в заданном формате
			exporterpdf.exportReport();
			exporterhtml.exportReport();
		}
	catch(JRException e)
		{
			log.error("Ошибка при создании отчёта.", e);
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			log.error("Ошибка при создании отчёта.", e);
			e.printStackTrace();
		}
	}
	
	//Классы исключений
	/** 
	 * Класс исключения, возникающего 
	 * при недостатке места на диске
	 * для сохранения записи
	 * */
	private class NotEnoughDiskSpace extends Exception
	{
		public NotEnoughDiskSpace()
		{
			super("Недостаточно места на диске для создания записи.");
		};
	};
	
	/**
	 * Класс исключения, возникающего,
	 * когда при заданных параметрах поиска
	 * не удаётся найти соответствующие группы.  
	 * */
	private class NotFound extends Exception
	{
		public NotFound()
		{
			super("Не найдены группы по заданным параметрам.");
		};
	};	
	
	//Класс, описывающий поток
	private class ThreadEx extends Thread {	
		
		//Тип потока
		private int type;
		
		//Задание имени потока и его приоритета
		public ThreadEx(int i) {
			type = i;
		}
		
		/** *Основной метод потока */
		public void run() {
			if(type == 1) {
				synchronized(mutex1) {					
					try {
						log.warn("Запущен поток, открывающий xml-файл.");
						XMLOpen();
					}
					catch(Exception e1) {
						log.error("Ошибка при запуске потока, открывающего xml-файл.", e1);
						e1.printStackTrace();
					}
					mutex1.notify();
					log.debug("Поток, открывающий xml-файл, завершил свою работу.");
				}
			}
			
			if(type == 2) {
				synchronized(mutex1) {					
					try {
						mutex1.wait();
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						log.warn("Запущен поток, сохраняющий xml-файл.");
						Thread.sleep(10000);
						
						XMLProcess();
					}
					catch(Exception e1) {
						log.error("Ошибка при запуске потока, сохраняющего xml-файл.", e1);
						e1.printStackTrace();
					}
				}
				
				synchronized(mutex2) {
					mutex2.notifyAll();
					log.debug("Поток, сохраняющий xml-файл, завершил свою работу.");
				}
			}
			
			if(type == 3) {
				synchronized(mutex2) {					
					try {
						mutex2.wait();
					}
					catch(InterruptedException e) {
						e.printStackTrace();
					}
					
					try {
						log.warn("Запущен поток, создающий отчёт.");
						Thread.sleep(20000);
						
						HTMLPDFReport();
					}
					catch(Exception e1) {
						log.error("Ошибка при запуске потока, создающего отчёт.", e1);
						e1.printStackTrace();
					}
					mutex2.notifyAll();
					log.debug("Поток, создающий отчёт, завершил свою работу.");
				}
			}
		}
	}
	
	//Объявления графических компонентов
	private JFrame InterForm;
	private DefaultTableModel Model;
	private JButton Open;
	private JButton Save;
	private JButton New;
	private JButton Edit;
	private JButton Delete;
	private JToolBar ToolBar;
	private JScrollPane Scroll;
	private JTable Groups; 
	private JComboBox Year;
	private JTextField GroupName;
	private JButton Filter;
	private JButton Report;
	
	private void CheckDiskSpace() throws NotEnoughDiskSpace
	{
		//if(Math.random() < 0.5)
			///throw new NotEnoughDiskSpace();
	};
	
	private void FindByParams() throws NotFound
	{
		//if(Math.random() >= 0.5)
			//throw new NotFound();
	};
	
	//Метод создания окна
	public void Show()
	{
		InterForm = new JFrame("Менеджер музыкальных групп");
		InterForm.setSize(500, 300);
		InterForm.setLocation(100, 100);
		InterForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//Создание кнопок и прикрепление иконок
		Open = new JButton(new ImageIcon("./img/open.png"));
		Save = new JButton(new ImageIcon("./img/save.png"));
		New = new JButton(new ImageIcon("./img/new.png"));
		Edit = new JButton(new ImageIcon("./img/edit.png"));
		Delete = new JButton(new ImageIcon("./img/delete.png"));
		Report = new JButton(new ImageIcon("./img/report.png"));
		
		//Настройка подсказок для кнопок
		Open.setToolTipText("Открыть");
		Save.setToolTipText("Сохранить");
		New.setToolTipText("Новая запись");
		Edit.setToolTipText("Редактировать запись");
		Delete.setToolTipText("Удалить запись");
		Report.setToolTipText("Создать отчёт");
		
		//Добавление кнопок на панель инструментов
		ToolBar = new JToolBar("Панель инструментов");
		ToolBar.add(Open);
		ToolBar.add(Save);
		ToolBar.add(New);
		ToolBar.add(Edit);
		ToolBar.add(Delete);
		ToolBar.add(Report);
		
		//Размещение панели инструментов
		InterForm.setLayout(new BorderLayout());
		InterForm.add(ToolBar, BorderLayout.NORTH);
		
		//Создание таблицы с данными
		String[] Columns = {"Группа", "Год основания", "Положение в хит-параде"};
		String[][] Data = {{"Paul Van Dyk", "1994", "105"}, {"ATB", "1998", "54"}};
		Model = new DefaultTableModel(Data, Columns);
		Groups = new JTable(Model);
		Scroll = new JScrollPane(Groups);		
		
		//Размещение таблицы с данными
		InterForm.add(Scroll, BorderLayout.CENTER);
		
		//Подготовка компонентов поиска
		Year = new JComboBox(new String[] {"Год", "1994", "1998"});		
		GroupName = new JTextField("Название группы");
		Filter = new JButton("Поиск");
		
		//Добавление компонентов на панель
		JPanel FilterPanel = new JPanel();
		FilterPanel.add(GroupName);
		FilterPanel.add(Year);
		FilterPanel.add(Filter);		
		
		//Размещение панели поиска внизу окна
		InterForm.add(FilterPanel, BorderLayout.SOUTH);
		
		//Визуализация экранной формы
		InterForm.setVisible(true);
		
		//Добавление слушателей
		New.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				log.debug("Была нажата кнопка \"Новая запись\".");
				
				/**Вывод сообщения в ответ на нажатие кнопки "Новая запись". */
				String group = "Doom";
				String year = "1993";
				String chart = "12";
				Model.addRow(new String[] {group, year, chart});
			}});
		
		Filter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				log.debug("Была нажата кнопка \"Поиск\".");
				
				/**Вывод сообщения в ответ на нажатие кнопки "Поиск". */
				try
				{
					FindByParams();
				}
				catch(NotFound ex)
				{
					JOptionPane.showMessageDialog(InterForm, "Не найдены группы с заданными параметрами поиска.");
				};
				JOptionPane.showMessageDialog(InterForm, "Проверка нажатия на кнопку \"Поиск\".");
			}});
		
		Year.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				log.debug("Был активирован раскрывающийся список \"Год\".");
				
				/**Вывод сообщения в ответ на выбор пункта в списке "Год". */
				JOptionPane.showMessageDialog(InterForm, "Проверка выбора пункта раскрывающегося списка \"Год\".");
			}});
		
		Open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
				log.debug("Была нажата кнопка \"Открыть\".");
				
				new ThreadEx(3).start();
				new ThreadEx(2).start();
				new ThreadEx(1).start();				
				
			}});
		
		Save.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				
				SavePressed = true;		
				log.debug("Была нажата кнопка \"Сохранить\".");
			}});
		
		//Генерируем отчёт
		Report.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				
				ReportPressed = true;
				log.debug("Была нажата кнопка \"Создать отчёт\".");
				
			}			
		});
	};	
	
	public static void main(String[] args)
	{
		//Настройка пути к файлу свойств Log4j
		try {
			p.load(new FileInputStream(Log4JPropertyFile));
			PropertyConfigurator.configure(p);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		log.info("Старт главного потока.");
		
		//Создание и отображение экранной формы
		new InterForm().Show();
	};
};

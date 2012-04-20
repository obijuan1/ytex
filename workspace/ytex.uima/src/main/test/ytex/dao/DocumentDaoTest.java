package ytex.dao;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.hibernate.SessionFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import ytex.model.Document;
import ytex.uima.ApplicationContextHolder;
import ytex.uima.mapper.DocumentMapperService;


public class DocumentDaoTest extends TestCase {
	DocumentDao documentDao = (DocumentDao) ApplicationContextHolder
			.getApplicationContext().getBean("documentDao");
//	DocumentAnnotationDao documentAnnotationDao = (DocumentAnnotationDao) ApplicationContextHolder
//			.getApplicationContext().getBean("documentAnnotationDao");
	SessionFactory sessionFactory = (SessionFactory) ApplicationContextHolder
			.getApplicationContext().getBean("sessionFactory");
	TransactionTemplate tx = new TransactionTemplate(
			(PlatformTransactionManager) ApplicationContextHolder
					.getApplicationContext().getBean("transactionManager"));

	public void testInit() {
		ApplicationContextHolder.getApplicationContext().getBean(
				DocumentMapperService.class);
	}
	public void testGetDocument() throws IOException {
		final int docId = 1;
		tx.execute(new TransactionCallback() {

			@Override
			public Object doInTransaction(TransactionStatus status) {
				try {
					Document doc = documentDao.getDocument(docId);
					System.out.println(doc);
					File dir = new File(System.getProperty("java.io.tmpdir")
							+ File.separatorChar + "esld");
					dir.mkdir();
					File file = new File(dir.getAbsolutePath()
							+ File.separatorChar + docId + ".xmi");
					file.createNewFile();
					OutputStream fos= new BufferedOutputStream(new FileOutputStream(file));
					if (doc != null && doc.getCas() != null) {
						InputStream is = new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(doc.getCas())));
						int b = -1;
						while((b = is.read()) > -1) {
							fos.write(b);
						}
					}
					fos.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				return null;
			}
		});

	}

//	public void testSaveDocument() {
//		Random ran = new Random();
//		final Document doc = new Document();
//		doc.setAnalysisBatch((new Date()).toString());
//		System.out.println(tx.execute(new TransactionCallback() {
//
//			@Override
//			public Object doInTransaction(TransactionStatus status) {
//				sessionFactory.getCurrentSession().save(doc);
//				SentenceAnnotation sent = new SentenceAnnotation();
//				sent.setSentenceNumber(1);
//				sent.setDocument(doc);
//				sent.setUimaType(documentAnnotationDao.getUimaTypeByName("edu.mayo.bmi.uima.core.sentence.type.Sentence"));
//				sessionFactory.getCurrentSession().save(sent);
////				sent = new SentenceAnnotation();
////				sent.setSentenceNumber(2);
////				sent.setDocument(doc);
////				sessionFactory.getCurrentSession().save(sent);
//				return doc.getDocumentID();
//			}
//		}));
//	}
	
//	public void testGetUimaTypeByName() {
//		System.out.println(documentAnnotationDao.getUimaTypeByName("edu.mayo.bmi.uima.core.sentence.type.Sentence"));		
//		System.out.println(documentAnnotationDao.getUimaTypeByName("edu.mayo.bmi.uima.core.sentence.type.Sentence"));		
//		System.out.println(documentAnnotationDao.getUimaTypeByName("edu.mayo.bmi.uima.core.ae.type.Segment"));
//	}

}

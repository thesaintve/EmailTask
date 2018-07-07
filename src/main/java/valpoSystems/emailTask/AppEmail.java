package valpoSystems.emailTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import valpoSystems.emailTask.orm.client.VariableInstanceLogMapper;
import valpoSystems.emailTask.orm.model.VariableInstanceLog;
import valpoSystems.emailTask.orm.model.VariableInstanceLogExample;
/**
 * Hello world!
 *
 */
public class AppEmail 
{
	
	public void enviarEmail(String subject, String mensaje, String destino) {
		final String username = "david.besson@valposystems.cl";
		final String password = "R1zGF/b6uO";
		
		Properties props = new Properties();
//		props.put("mail.smtp.auth", "true"); // mail.valposystems.cl
//		props.put("mail.smtp.starttls.enable", "true");
//		props.put("mail.smtp.host", "smtp.gmail.com");
//		props.put("mail.smtp.port", "587");

		props.put("mail.smtp.auth", "true"); 
		//props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "mail.valposystems.cl");
		props.put("mail.smtp.port", "587");
		
		
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		  });

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress("sistema@jbpm.com"));
//			message.setFrom(new InternetAddress("david.besson@gmail.com"));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destino));
			message.setSubject(subject);
			message.setText(mensaje);

			Transport.send(message);

			System.out.println("Enviado");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SqlSession getSession() {
		SqlSessionFactory sqlSessionFactory = null;
		SqlSession sqlSession = null;
		try {
//			String resource = "ValpoSystems/EmailTask/jbpm.conexion.xml";
			String resource = "valpoSystems/emailTask/jbpm.conexion.xml";
//			InputStream inputStream;
//			inputStream = Resources.getResourceAsStream(resource);
			Reader reader = Resources.getResourceAsReader (resource);
			
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
			//sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			sqlSession = sqlSessionFactory.openSession();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sqlSession;
	}
	
	public String cagaVariables(SqlSession sqlSession, String processId, String processInstanceId, String variableId) {
		//SqlSession sqlSession = getSession();		
		VariableInstanceLogExample filtro = new VariableInstanceLogExample();
		filtro.createCriteria().andProcessidEqualTo(processId).andProcessinstanceidEqualTo(Long.valueOf(processInstanceId)).andVariableidEqualTo(variableId);
		List<VariableInstanceLog> lstVariables = sqlSession.getMapper(VariableInstanceLogMapper.class).selectByExample(filtro);
		if (lstVariables!=null && lstVariables.size()>0) {
			VariableInstanceLog vilog = lstVariables.get(0);
			return vilog.getValue();
		}
		return null;
	}
	
	
    public static void main(String[] args) {
		AppEmail app = new AppEmail();
    	
        System.out.println( args[0] );
        System.out.println( args[1] );
        String processId = args[0];
        String processInstanceId = args[1];
        
		SqlSession sqlSession = app.getSession();	
		String emailSubject = app.cagaVariables(sqlSession, processId, processInstanceId, "emailsubject");
		String emailMessage = app.cagaVariables(sqlSession, processId, processInstanceId, "emailmessage");
		String emailAddress = app.cagaVariables(sqlSession, processId, processInstanceId, "emailaddress");
		
		app.enviarEmail(emailSubject, emailMessage, emailAddress);		
    }
    
    
}

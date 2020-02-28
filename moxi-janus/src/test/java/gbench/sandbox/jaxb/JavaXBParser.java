package gbench.sandbox.jaxb;

import java.io.File;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import com.sun.xml.txw2.annotation.XmlElement;
import lombok.ToString;

public class JavaXBParser {
    
    @ToString
    @XmlElement()
    public static class Select{
        @XmlAttribute()
        public String id;
    }
    
    @ToString
    @XmlRootElement()
    public static class Mapper{
        @XmlAttribute()
        public String namespace;
        @XmlElementWrapper(name = "mapper")
        private List<Select> selects;
    }
    
    public static Mapper get(String path) {
        try {
            File file = new File(path);
            
            JAXBContext jc = JAXBContext.newInstance(Mapper.class);
            Unmarshaller jaxbUnmarshaller = jc.createUnmarshaller(); 
            Mapper mapper = (Mapper)jaxbUnmarshaller.unmarshal(file);
            return (mapper);
            
        } catch (JAXBException e) {
           e.printStackTrace();
        }
        return null;
    }
    public static void main(String args[]) {
        final var file= "D:/sliced/sandbox/saas-hp/marketingplatform/src/main/resources/mapper/UserMapper.xml";
        System.out.println(get(file));
    }

}

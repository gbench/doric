package gbench.sandbox.jaxb;

import org.junit.Test;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomParser {
    public static void element(NodeList list){
        for (int i = 0; i <list.getLength() ; i++) {
            Element element = (Element) list.item(i);
            NodeList childNodes = element.getChildNodes();
            for (int j = 0; j <childNodes.getLength() ; j++) {
                if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
                    //��ȡ�ڵ�
                    System.out.print(childNodes.item(j).getNodeName() + ":");
                    //��ȡ�ڵ�ֵ
                    System.out.println(childNodes.item(j).getFirstChild().getNodeValue());
                }
            }
        }
    }

    public static void node(NodeList list){
        for (int i = 0; i <list.getLength() ; i++) {
            Node node = list.item(i);
            NodeList childNodes = node.getChildNodes();
            for (int j = 0; j <childNodes.getLength() ; j++) {
                if (childNodes.item(j).getNodeType()==Node.ELEMENT_NODE) {
                    var item = childNodes.item(j);
                    System.out.print(item.getNodeName() + ":");
                    var attrs = item.getAttributes();
                    System.out.println(attrs.getNamedItem("id"));
                    System.out.println(item.getFirstChild().getNodeValue());
                }
            }
        }
    }
    
    
    public static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    public static void parse(String path) {
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document d = builder.parse(path);
            NodeList sList = d.getElementsByTagName("mapper");// ��ȡ���ڵ�
            node(sList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void foo() {
        parse("D:\\sliced\\sandbox\\saas-hp\\marketingplatform\\src\\main\\resources\\mapper\\OrderMapper.xml");
    }
    
}

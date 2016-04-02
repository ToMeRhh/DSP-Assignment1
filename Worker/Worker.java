package ass1;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;



public class Worker {
	private class KV<F,S>{
		F f;
		S s;
	}
	private static String APIUrl = "https://kgsearch.googleapis.com/v1/entities:search?key=AIzaSyBA0QFiW2iZtNmhrwf-YRRQSVimKA8Y8G8&query=";
	static StanfordCoreNLP  sentimentPipeline;
	static StanfordCoreNLP EntitiesPipeline;
	
	public static String getTweet(String url) throws IOException {
		Document doc = Jsoup.connect(url).get();
		return doc.title().split("\"")[1];
	}
	
	public static Map<String, String> getEntities(String tweet){
		Map<String, String> ret = new HashMap<String, String>();
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(tweet);
 
        // run all Annotators on this text
        EntitiesPipeline.annotate(document);
 
        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        System.out.println("Entities: ");
        int i = 1;
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                if (ne.equals("PERSON") || ne.equals("ORGANIZATION") || ne.equals("LOCATION")) {
                	System.out.println("\t" + i++ + " -" + word + ":" + ne);
                	ret.put(word, ne);
                }
                	
            }
        }
 
        return ret;
    }
	
	public static int findSentiment(String tweet) {
		 		
		
        int mainSentiment = 0;
        if (tweet != null && tweet.length() > 0) {
            int longest = 0;
            Annotation annotation = sentimentPipeline.process(tweet);
            for (CoreMap sentence : annotation
                    .get(CoreAnnotations.SentencesAnnotation.class)) {
                Tree tree = sentence
                        .get(SentimentCoreAnnotations.AnnotatedTree.class);
                int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
                String partText = sentence.toString();
                if (partText.length() > longest) {
                    mainSentiment = sentiment;
                    longest = partText.length();
                }
 
            }
        }
        return mainSentiment;
	}
	
	public static void main(String[] args) throws IOException {
		
		// Init Entities Pipeline
		Properties AnnotProps = new Properties();
		AnnotProps.put("annotators", "tokenize, ssplit, parse, sentiment");
		sentimentPipeline =  new StanfordCoreNLP(AnnotProps);
		
		// Init Entities Pipeline
		Properties EntProps = new Properties();
		EntProps.put("annotators", "tokenize , ssplit, pos, lemma, ner");
		EntitiesPipeline =  new StanfordCoreNLP(EntProps);
		
		// Done Init.

		// init tweet list:
		String[] tweets = {
				"https://www.twitter.com/BarackObama/status/710517154987122689",
				"https://www.twitter.com/realDonaldTrump/status/711388380668493824",
				"https://www.twitter.com/haggard_metal/status/672309330608287744",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712066362689130497",
				"https://www.twitter.com/haggard_metal/status/665377834664337408",
				"https://www.twitter.com/Columbia/status/711964403407704065",
				"https://www.twitter.com/AABGU/status/677185552811237376",
				"https://www.twitter.com/PearlJam/status/708778370838847489",
				"https://www.twitter.com/HulkHogan/status/710762613668503553",
				"https://www.twitter.com/realDonaldTrump/status/712291375958659072",
				"https://www.twitter.com/Perspective_pic/status/711003771015438336",
				"https://www.twitter.com/TristaniaBand/status/578527672105627648",
				"https://www.twitter.com/bengurionu/status/706464195345256449",
				"https://www.twitter.com/SagerNotebook/status/712287090382397440",
				"https://www.twitter.com/HillaryClinton/status/712092743032504320",
				"https://www.twitter.com/PearlJam/status/709074072454307840",
				"https://www.twitter.com/BarackObama/status/711239296334712833",
				"https://www.twitter.com/Perspective_pic/status/712090927385485313",
				"https://www.twitter.com/GeorgeTakei/status/712190086792617984",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712156976650829824",
				"https://www.twitter.com/realDonaldTrump/status/711653804983361536",
				"https://www.twitter.com/haggard_metal/status/655046904162754560",
				"https://www.twitter.com/HillaryClinton/status/712004840025690115",
				"https://www.twitter.com/AABGU/status/684472097356251136",
				"https://www.twitter.com/PearlJam/status/709133197032853504",
				"https://www.twitter.com/TweetsofOld/status/712116171135262720",
				"https://www.twitter.com/TristaniaBand/status/654644797911777280",
				"https://www.twitter.com/BernieSanders/status/712097053019537409",
				"https://www.twitter.com/PearlJam/status/710873490383962113",
				"https://www.twitter.com/reddit/status/710205106801618944",
				"https://www.twitter.com/PearlJam/status/711677476284411904",
				"https://www.twitter.com/realDonaldTrump/status/711586556688146434",
				"https://www.twitter.com/aaronpaul_8/status/707646859108597760",
				"https://www.twitter.com/TweetsofOld/status/711399889326833664",
				"https://www.twitter.com/AABGU/status/661233953035128832",
				"https://www.twitter.com/HulkHogan/status/711009923690323968",
				"https://www.twitter.com/HulkHogan/status/708242457549520897",
				"https://www.twitter.com/BarackObama/status/710577503329341440",
				"https://www.twitter.com/TristaniaBand/status/532682679469502464",
				"https://www.twitter.com/Perspective_pic/status/711393371181486080",
				"https://www.twitter.com/GameOfThrones/status/711673704158076928",
				"https://www.twitter.com/Columbia/status/712263921202302977",
				"https://www.twitter.com/haggard_metal/status/661622841771692032",
				"https://www.twitter.com/SagerNotebook/status/706027007419641856",
				"https://www.twitter.com/PearlJam/status/711277317931204608",
				"https://www.twitter.com/TweetsofOld/status/711998729344028672",
				"https://www.twitter.com/NASA/status/711975870307700737",
				"https://www.twitter.com/SagerNotebook/status/709369320074317825",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712187219730157568",
				"https://www.twitter.com/Perspective_pic/status/709943790434459649",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711160646243590145",
				"https://www.twitter.com/BarackObama/status/710856454333288448",
				"https://www.twitter.com/Perspective_pic/status/711728560382320640",
				"https://www.twitter.com/Perspective_pic/status/711317867178938369",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711069902170361856",
				"https://www.twitter.com/TristaniaBand/status/554788399568416768",
				"https://www.twitter.com/SagerNotebook/status/710564123378118656",
				"https://www.twitter.com/SagerNotebook/status/705951416162988032",
				"https://www.twitter.com/Perspective_pic/status/711680266113261568",
				"https://www.twitter.com/HillaryClinton/status/712093345049403392",
				"https://www.twitter.com/Perspective_pic/status/712099982275387393",
				"https://www.twitter.com/BarackObama/status/710587834680233984",
				"https://www.twitter.com/bengurionu/status/708971559357161472",
				"https://www.twitter.com/AABGU/status/664914367503335425",
				"https://www.twitter.com/haggard_metal/status/662657091182723072",
				"https://www.twitter.com/haggard_metal/status/655046892766760960",
				"https://www.twitter.com/BarackObama/status/709809198314364929",
				"https://www.twitter.com/aaronpaul_8/status/710232601626284032",
				"https://www.twitter.com/realDonaldTrump/status/711626493626093573",
				"https://www.twitter.com/bengurionu/status/706440801384079361",
				"https://www.twitter.com/realDonaldTrump/status/711638301111939072",
				"https://www.twitter.com/SagerNotebook/status/710450992559611905",
				"https://www.twitter.com/AABGU/status/669547978827415553",
				"https://www.twitter.com/reddit/status/712196214066774016",
				"https://www.twitter.com/bengurionu/status/702832898857168896",
				"https://www.twitter.com/HillaryClinton/status/711943781277442049",
				"https://www.twitter.com/reddit/status/709820477292683265",
				"https://www.twitter.com/TweetsofOld/status/711591251569541120",
				"https://www.twitter.com/bengurionu/status/707127177993658368",
				"https://www.twitter.com/GeorgeTakei/status/712058430148763648",
				"https://www.twitter.com/GeorgeTakei/status/712054207910522884",
				"https://www.twitter.com/Perspective_pic/status/710230710632710146",
				"https://www.twitter.com/AABGU/status/671715812248547328",
				"https://www.twitter.com/Columbia/status/712083927691821056",
				"https://www.twitter.com/HillaryClinton/status/712093688009256960",
				"https://www.twitter.com/GeorgeTakei/status/712114595997986816",
				"https://www.twitter.com/GeorgeTakei/status/712093064618184705",
				"https://www.twitter.com/BernieSanders/status/712045631661940736",
				"https://www.twitter.com/GameOfThrones/status/711955555913482241",
				"https://www.twitter.com/TristaniaBand/status/596536448855203840",
				"https://www.twitter.com/GeorgeTakei/status/712129695316217856",
				"https://www.twitter.com/bengurionu/status/704595954519941121",
				"https://www.twitter.com/NASA/status/711943165641502720",
				"https://www.twitter.com/realDonaldTrump/status/711350170798178304",
				"https://www.twitter.com/haggard_metal/status/673034116238073856",
				"https://www.twitter.com/reddit/status/710547663444254720",
				"https://www.twitter.com/HillaryClinton/status/711697489993150465",
				"https://www.twitter.com/AABGU/status/694632054479327232",
				"https://www.twitter.com/realDonaldTrump/status/711414965224259586",
				"https://www.twitter.com/haggard_metal/status/672671713025478656",
				"https://www.twitter.com/haggard_metal/status/661220576179134464",
				"https://www.twitter.com/PearlJam/status/711632663170326529",
				"https://www.twitter.com/TristaniaBand/status/539234637365919744",
				"https://www.twitter.com/haggard_metal/status/662270691568435200",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712126756606582784",
				"https://www.twitter.com/Perspective_pic/status/710641401604734976",
				"https://www.twitter.com/BernieSanders/status/712025841220321284",
				"https://www.twitter.com/PearlJam/status/711228270683955200",
				"https://www.twitter.com/TweetsofOld/status/711943157852676096",
				"https://www.twitter.com/GeorgeTakei/status/712099505789739008",
				"https://www.twitter.com/NASA/status/711667968070041600",
				"https://www.twitter.com/Perspective_pic/status/711366171422040064",
				"https://www.twitter.com/TweetsofOld/status/711694024847990784",
				"https://www.twitter.com/AABGU/status/701869839997128706",
				"https://www.twitter.com/PearlJam/status/711979476406509569",
				"https://www.twitter.com/HillaryClinton/status/712289758521782273",
				"https://www.twitter.com/SagerNotebook/status/710571865178312705",
				"https://www.twitter.com/GameOfThrones/status/711960597751943169",
				"https://www.twitter.com/TweetsofOld/status/712016995344986112",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711341619530878977",
				"https://www.twitter.com/HillaryClinton/status/712092398898257920",
				"https://www.twitter.com/Columbia/status/712063801248653313",
				"https://www.twitter.com/PearlJam/status/710548779984814080",
				"https://www.twitter.com/TweetsofOld/status/711738164776296449",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711221150399987712",
				"https://www.twitter.com/Perspective_pic/status/710955475651698690",
				"https://www.twitter.com/BarackObama/status/710119666937499649",
				"https://www.twitter.com/bengurionu/status/702741745042395137",
				"https://www.twitter.com/bengurionu/status/699132222444564480",
				"https://www.twitter.com/NASA/status/712006386096480256",
				"https://www.twitter.com/NASA/status/711983703875047424",
				"https://www.twitter.com/haggard_metal/status/656461411854479360",
				"https://www.twitter.com/bengurionu/status/702075106038833153",
				"https://www.twitter.com/GameOfThrones/status/711703877708435457",
				"https://www.twitter.com/BarackObama/status/711915519687917568",
				"https://www.twitter.com/realDonaldTrump/status/711521532149895168",
				"https://www.twitter.com/Perspective_pic/status/710278988858597377",
				"https://www.twitter.com/PearlJam/status/710923000854413312",
				"https://www.twitter.com/GeorgeTakei/status/712174983049392128",
				"https://www.twitter.com/NASA/status/712078845013336069",
				"https://www.twitter.com/HulkHogan/status/710025802482655232",
				"https://www.twitter.com/GeorgeTakei/status/712069285271580672",
				"https://www.twitter.com/GeorgeTakei/status/712251752922292230",
				"https://www.twitter.com/SagerNotebook/status/705794139691024384",
				"https://www.twitter.com/GeorgeTakei/status/712077066020196352",
				"https://www.twitter.com/TweetsofOld/status/711560347417595904",
				"https://www.twitter.com/realDonaldTrump/status/711349494701494272",
				"https://www.twitter.com/SagerNotebook/status/706142248031768580",
				"https://www.twitter.com/aaronpaul_8/status/706163020355883008",
				"https://www.twitter.com/HillaryClinton/status/712280152944865281",
				"https://www.twitter.com/BernieSanders/status/712086034834694144",
				"https://www.twitter.com/BarackObama/status/710913733371899904",
				"https://www.twitter.com/PearlJam/status/712036126593142784",
				"https://www.twitter.com/TweetsofOld/status/712043422127923200",
				"https://www.twitter.com/Perspective_pic/status/709916599776624640",
				"https://www.twitter.com/BernieSanders/status/712012616323997696",
				"https://www.twitter.com/realDonaldTrump/status/712291134991691777",
				"https://www.twitter.com/TweetsofOld/status/711629138772107264",
				"https://www.twitter.com/SagerNotebook/status/710585092226359296",
				"https://www.twitter.com/GeorgeTakei/status/712220302009217025",
				"https://www.twitter.com/realDonaldTrump/status/712248639947730944",
				"https://www.twitter.com/Perspective_pic/status/710593078000803841",
				"https://www.twitter.com/HulkHogan/status/707507209589153794",
				"https://www.twitter.com/BarackObama/status/710546185480642560",
				"https://www.twitter.com/reddit/status/710563762302914561",
				"https://www.twitter.com/HulkHogan/status/707878729498935296",
				"https://www.twitter.com/PearlJam/status/710596585869037568",
				"https://www.twitter.com/haggard_metal/status/672309326556569600",
				"https://www.twitter.com/Columbia/status/711983715572908033",
				"https://www.twitter.com/haggard_metal/status/711896497873428480",
				"https://www.twitter.com/BarackObama/status/709830400474939392",
				"https://www.twitter.com/realDonaldTrump/status/711366483872522240",
				"https://www.twitter.com/GameOfThrones/status/709773658659495941",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712217508162441216",
				"https://www.twitter.com/GeorgeTakei/status/712258570704834561",
				"https://www.twitter.com/BernieSanders/status/711991295548399618",
				"https://www.twitter.com/haggard_metal/status/673396505840103424",
				"https://www.twitter.com/BernieSanders/status/711994299810852865",
				"https://www.twitter.com/BEAUTIFULPlCS/status/710979461995429891",
				"https://www.twitter.com/Columbia/status/712008439040442368",
				"https://www.twitter.com/BernieSanders/status/712004378748538884",
				"https://www.twitter.com/realDonaldTrump/status/712235345287516160",
				"https://www.twitter.com/reddit/status/709864886898020352",
				"https://www.twitter.com/GeorgeTakei/status/712159885350154240",
				"https://www.twitter.com/bengurionu/status/710022742041563138",
				"https://www.twitter.com/HulkHogan/status/712215547816058881",
				"https://www.twitter.com/HulkHogan/status/709486693468995585",
				"https://www.twitter.com/HillaryClinton/status/712087754323001345",
				"https://www.twitter.com/TweetsofOld/status/711721015101894656",
				"https://www.twitter.com/AABGU/status/661933167138512896",
				"https://www.twitter.com/realDonaldTrump/status/711520501856866304",
				"https://www.twitter.com/BarackObama/status/710208575512158208",
				"https://www.twitter.com/SagerNotebook/status/710117149155696640",
				"https://www.twitter.com/aaronpaul_8/status/707375636726341632",
				"https://www.twitter.com/PearlJam/status/709783480305983488",
				"https://www.twitter.com/Perspective_pic/status/710668655059722242",
				"https://www.twitter.com/HillaryClinton/status/712114145060134912",
				"https://www.twitter.com/haggard_metal/status/709445368182906881",
				"https://www.twitter.com/reddit/status/710243495080894466",
				"https://www.twitter.com/GameOfThrones/status/712036068833374208",
				"https://www.twitter.com/SagerNotebook/status/711979469695754240",
				"https://www.twitter.com/HillaryClinton/status/712279889353768961",
				"https://www.twitter.com/HillaryClinton/status/711983231399305216",
				"https://www.twitter.com/GeorgeTakei/status/712039620633088000",
				"https://www.twitter.com/aaronpaul_8/status/704037070344314880",
				"https://www.twitter.com/GameOfThrones/status/711990783046356992",
				"https://www.twitter.com/HillaryClinton/status/711721323148390400",
				"https://www.twitter.com/PearlJam/status/708727527330582528",
				"https://www.twitter.com/AABGU/status/704387881947832320",
				"https://www.twitter.com/BarackObama/status/710098768276787200",
				"https://www.twitter.com/reddit/status/710219328742760448",
				"https://www.twitter.com/aaronpaul_8/status/702677954946428928",
				"https://www.twitter.com/BarackObama/status/712030379109036033",
				"https://www.twitter.com/NASA/status/711953398728253440",
				"https://www.twitter.com/PearlJam/status/709423982525583362",
				"https://www.twitter.com/realDonaldTrump/status/711418254556852224",
				"https://www.twitter.com/BernieSanders/status/712106027571744768",
				"https://www.twitter.com/GameOfThrones/status/712002104768921600",
				"https://www.twitter.com/NASA/status/712046885733867520",
				"https://www.twitter.com/GeorgeTakei/status/712144792906440704",
				"https://www.twitter.com/SagerNotebook/status/707300073273593856",
				"https://www.twitter.com/BarackObama/status/710874353068421124",
				"https://www.twitter.com/AABGU/status/662653083676254209",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712278062432456705",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711281243195891713",
				"https://www.twitter.com/GameOfThrones/status/711998071358402561",
				"https://www.twitter.com/Columbia/status/712046180650323968",
				"https://www.twitter.com/TristaniaBand/status/547924038669307904",
				"https://www.twitter.com/HulkHogan/status/708053343856365568",
				"https://www.twitter.com/SagerNotebook/status/709435679420358657",
				"https://www.twitter.com/PearlJam/status/710139109759561729",
				"https://www.twitter.com/BernieSanders/status/712081414498480129",
				"https://www.twitter.com/NASA/status/712026755020791811",
				"https://www.twitter.com/AABGU/status/667461358536007680",
				"https://www.twitter.com/HulkHogan/status/710272901325389825",
				"https://www.twitter.com/AABGU/status/674718230347448320",
				"https://www.twitter.com/TristaniaBand/status/655005998080462848",
				"https://www.twitter.com/SagerNotebook/status/708403070644973569",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711039686081429504",
				"https://www.twitter.com/AABGU/status/708027527021912065",
				"https://www.twitter.com/realDonaldTrump/status/711742734508421120",
				"https://www.twitter.com/haggard_metal/status/672309390041604096",
				"https://www.twitter.com/PearlJam/status/709836481716752384",
				"https://www.twitter.com/TweetsofOld/status/711505535686696961",
				"https://www.twitter.com/SagerNotebook/status/705855548978286592",
				"https://www.twitter.com/Columbia/status/711976995786268672",
				"https://www.twitter.com/BarackObama/status/711277342832746496",
				"https://www.twitter.com/HulkHogan/status/711850478422921216",
				"https://www.twitter.com/HulkHogan/status/710899185768382467",
				"https://www.twitter.com/Columbia/status/712268912319188993",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711009603845230593",
				"https://www.twitter.com/bengurionu/status/699892106878394368",
				"https://www.twitter.com/haggard_metal/status/676188601055641600",
				"https://www.twitter.com/GeorgeTakei/status/712205188115726336",
				"https://www.twitter.com/reddit/status/710968629903294468",
				"https://www.twitter.com/TweetsofOld/status/711678732759511040",
				"https://www.twitter.com/HillaryClinton/status/711917832909537280",
				"https://www.twitter.com/HulkHogan/status/710988297284292608",
				"https://www.twitter.com/haggard_metal/status/662994513900781568",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711100135376289792",
				"https://www.twitter.com/Columbia/status/711993383712112641",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712247795076440064",
				"https://www.twitter.com/NASA/status/712062234214723584",
				"https://www.twitter.com/realDonaldTrump/status/711353589394972674",
				"https://www.twitter.com/reddit/status/710613542760677376",
				"https://www.twitter.com/HillaryClinton/status/712069310265556993",
				"https://www.twitter.com/bengurionu/status/699215680827224064",
				"https://www.twitter.com/AABGU/status/659039313401876480",
				"https://www.twitter.com/Perspective_pic/status/711755719645782017",
				"https://www.twitter.com/reddit/status/710884065063231488",
				"https://www.twitter.com/TweetsofOld/status/711995059596464129",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711251310214041600",
				"https://www.twitter.com/SagerNotebook/status/706028638756450304",
				"https://www.twitter.com/BernieSanders/status/712065528517427200",
				"https://www.twitter.com/haggard_metal/status/672309331711426560",
				"https://www.twitter.com/SagerNotebook/status/705997138019246080",
				"https://www.twitter.com/reddit/status/709792641907433472",
				"https://www.twitter.com/HillaryClinton/status/712036328268034049",
				"https://www.twitter.com/realDonaldTrump/status/711639836810203142",
				"https://www.twitter.com/TweetsofOld/status/711863571504766976",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712039870030553089",
				"https://www.twitter.com/BEAUTIFULPlCS/status/711190935892320256",
				"https://www.twitter.com/Columbia/status/712278367886827520",
				"https://www.twitter.com/Perspective_pic/status/712118144689704960",
				"https://www.twitter.com/TweetsofOld/status/711611517221797888",
				"https://www.twitter.com/GeorgeTakei/status/712084404797964288",
				"https://www.twitter.com/SagerNotebook/status/706914920890298369",
				"https://www.twitter.com/AABGU/status/698185971217600513",
				"https://www.twitter.com/GeorgeTakei/status/712111357966082050",
				"https://www.twitter.com/BernieSanders/status/712029436942622721",
				"https://www.twitter.com/realDonaldTrump/status/711631122036293632",
				"https://www.twitter.com/haggard_metal/status/671946919623720961",
				"https://www.twitter.com/AABGU/status/691727242347569153",
				"https://www.twitter.com/GameOfThrones/status/711641732945842176",
				"https://www.twitter.com/BarackObama/status/711972687489314816",
				"https://www.twitter.com/BernieSanders/status/711992712694796288",
				"https://www.twitter.com/TweetsofOld/status/712098566802571266",
				"https://www.twitter.com/BarackObama/status/710935059583279104",
				"https://www.twitter.com/HillaryClinton/status/712099377553125376",
				"https://www.twitter.com/Columbia/status/712284947365629953",
				"https://www.twitter.com/AABGU/status/686995664039972869",
				"https://www.twitter.com/AABGU/status/659055861730557952",
				"https://www.twitter.com/Columbia/status/712014728177045506",
				"https://www.twitter.com/HulkHogan/status/710403031758741504",
				"https://www.twitter.com/HulkHogan/status/707512825950953476",
				"https://www.twitter.com/BernieSanders/status/712001674622210048",
				"https://www.twitter.com/NASA/status/711699593151569920",
				"https://www.twitter.com/NASA/status/711966836049514496",
				"https://www.twitter.com/GeorgeTakei/status/712235396512395266",
				"https://www.twitter.com/SagerNotebook/status/709370028798435328",
				"https://www.twitter.com/reddit/status/710171118670204928",
				"https://www.twitter.com/HulkHogan/status/710518941236523009",
				"https://www.twitter.com/PearlJam/status/710215326705672192",
				"https://www.twitter.com/BEAUTIFULPlCS/status/712096563074605057",
				"https://www.twitter.com/TweetsofOld/status/711656472740900864",
				"https://www.twitter.com/NASA/status/712056194861555712",
				"https://www.twitter.com/reddit/status/710538895562186752",
				"https://www.twitter.com/aaronpaul_8/status/707381590624759808",
				"https://www.twitter.com/Perspective_pic/status/711030938646532096",
				"https://www.twitter.com/Perspective_pic/status/710306168401469440"
		};
		
		for(String tweet : tweets)
			handleTweet(tweet);
	}
	private static void handleTweet(String tweetURL) {
		String tweet;
		try {
			tweet = getTweet(tweetURL);
			System.out.println("******************************************");
			System.out.println(tweet);
			System.out.println("******************************************");
			System.out.println("Sentiment: " + findSentiment(tweet));
			
			Map<String, String> entities = getEntities(tweet);
			Map<String, KV<String, JSONArray> > linkedEntities = new HashMap<String, KV<String, JSONArray>>();
			for (String entity : entities.keySet()){
				
				System.out.println(entity);
				
				String jsonResponse = getKnowladge(entity);
				JSONParser parser = new JSONParser();

				try {

					Object obj = parser.parse(new StringReader(jsonResponse));
					JSONObject jsonObject = (JSONObject) obj;
					
					System.out.println("Getting Array:");
					JSONArray results = (JSONArray)jsonObject.get("itemListElement");
					Iterator<JSONObject> iterator = results.iterator();
					if (iterator.hasNext()) {
						JSONObject result = iterator.next();
						result = (JSONObject) result.get("result");	
						System.out.println(result.get("name"));
						result = (JSONObject) result.get("detailedDescription");
						System.out.println(result.get("url"));
					}

				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static String getKnowladge(String entity) throws IOException {
		return Jsoup.connect(APIUrl + entity).ignoreContentType(true).execute().body();
	}

}
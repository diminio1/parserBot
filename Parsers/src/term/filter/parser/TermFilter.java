package term.filter.parser;

import pair.parser.Pair;

import java.util.ArrayList;
import java.util.List;

import java.util.regex.*;

/**
 *
 * @author elena
 */
public class TermFilter 
{
    /*
        List<ID, List<term>>
    */
    private List<Pair<Pair<Integer, Integer>, List<String>>> termsWithIdsList;

    public TermFilter() {}            

    public TermFilter(List<Pair<Pair<Integer, Integer>, List<String>>> termsWithIdsList) 
    {
        this.termsWithIdsList = termsWithIdsList;
    }

    public List<Pair<Pair<Integer, Integer>, List<String>>> getTermsWithIdsList() 
    {
        return termsWithIdsList;
    }
    public void setTerms(List<Pair<Pair<Integer, Integer>, List<String>>> termsWithIdsList) 
    {
        this.termsWithIdsList = termsWithIdsList;
    }

    
       
    public List<Pair<Integer, Integer>> filter(String inputString)
    {
        if (inputString != null && !inputString.isEmpty() && termsWithIdsList != null)
        {            
            List<Pair<Integer, Integer>> res = new ArrayList<>();
            
            for (Pair<Pair<Integer, Integer>, List<String>> tokenPair : termsWithIdsList)
            {
                List<String> terms = tokenPair != null ? tokenPair.getSecond() : null;
                if (terms != null)
                {
                    for (String term : terms)
                    {
                    	if (!term.equals("") && !term.equals(" ")) {
                    		
                    		
	                        if (inputString.contains(term))
	                        {
	                        	String regex = "\\b" + term + "\\b";
	                        	if(Pattern.compile(regex).matcher(inputString).find()){	                        		
	                        		res.add(new Pair <Integer, Integer> (tokenPair.getFirst().getFirst(), tokenPair.getFirst().getSecond()));
	                        		break;
	                        	}
	                        }
                    	}
                    }
                }                
            }
            
            //TODO print if problem with city!!!
            
            return !res.isEmpty() ? res : null;
        }
        
        return null;
    }
}

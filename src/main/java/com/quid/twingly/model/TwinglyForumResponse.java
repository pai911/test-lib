package com.quid.twingly.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.io.Serializable;

@Data
public class TwinglyForumResponse implements Serializable {

	@JsonProperty("number_of_matches_returned")
	private int numberOfMatchesReturned;

	@JsonProperty("number_of_matches_total")
	private int numberOfMatchesTotal;

	@JsonProperty("incomplete_result")
	private boolean incompleteResult;

	@JsonProperty("documents")
	private List<TwinglyForumDocument> documents;

	@Override
 	public String toString(){
		return 
			"ResponseDTO{" + 
			"number_of_matches_returned = '" + numberOfMatchesReturned + '\'' + 
			",number_of_matches_total = '" + numberOfMatchesTotal + '\'' + 
			",incomplete_result = '" + incompleteResult + '\'' + 
			",documents = '" + documents + '\'' + 
			"}";
		}
}
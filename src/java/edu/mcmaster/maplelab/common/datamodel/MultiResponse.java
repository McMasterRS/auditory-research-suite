package edu.mcmaster.maplelab.common.datamodel;


public class MultiResponse {
	private final Response<?>[] _responses;
	
	public MultiResponse(int count) {
		_responses = new Response<?>[count];
	}
	
	public void setResponse(int index, Response<?> response) {
		_responses[index] = response;
	}
	
	public Response<?> getResponse(int index) {
		return _responses[index];
	}
	
	@Override
	public String toString() {
		String str = "{";
		for (Response<?> r : _responses) {
			str += r.toString() + "; ";
		}
		return str.substring(0, str.length()-2) + "}";
	}
}

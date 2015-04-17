package uk.ac.imperial.pipe.models.petrinet;

import uk.ac.imperial.pipe.runner.JsonParameters;
import uk.ac.imperial.pipe.runner.PlaceMarker;
import uk.ac.imperial.pipe.visitor.component.PetriNetComponentVisitor;

public class DiscreteExternalTransition extends AbstractTransition implements Transition, ExternalTransitionProvider {

	private Class<ExternalTransition> clientClass;
	private ExternalTransition client;
	private String className;
	private PlaceMarker placeMarker;
	private Object context;

	public DiscreteExternalTransition(String id, String name, String className) {
		super(id, name);
		this.className = className; 
		buildClient(); 
	}

	public DiscreteExternalTransition(DiscreteExternalTransition transition) {
		super(transition);
		this.className = transition.className; 
		buildClient(); 
	}
	

	@SuppressWarnings("unchecked")
	private void buildClient() {
		if (this.className == null) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class name not specified.");
		}
		try {
			this.clientClass = (Class<ExternalTransition>) Class.forName(this.className);
			client = this.clientClass.newInstance(); 
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not exist: "+this.className+"\n"+e.getMessage());
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not implement uk.ac.imperial.pipe.models.petrinet.ExternalTransition: "+this.className+"\n"+e.getMessage());
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not have a null constructor: "+this.className+"\n"+e.getMessage());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("DiscreteExternalTransition.init:  client class does not have a public constructor: "+this.className+"\n"+e.getMessage());
		}
	}


	/**
     * visits the visitor of it is a {@link uk.ac.imperial.pipe.models.petrinet.DiscreteTransitionVisitor} or a
     * {@link uk.ac.imperial.pipe.models.petrinet.TransitionVisitor}.
     * @param visitor
     */
    @Override
    public void accept(PetriNetComponentVisitor visitor) {
        if (visitor instanceof TransitionVisitor) {
            ((TransitionVisitor) visitor).visit(this);
        }
        if (visitor instanceof DiscreteExternalTransitionVisitor) {
            ((DiscreteExternalTransitionVisitor) visitor).visit(this);
        }
    }

	@Override
	public void fire() {
		client.setExternalTransitionProvider(this); 
		client.fire(); 
	}

	public ExternalTransition getClient() {
		return client;
	}
	public void setPlaceMarker(PlaceMarker placeMarker) {
		this.placeMarker = placeMarker;
	}
	public void setContextForClient(Object context) {
		if (getClient() instanceof TransitionJsonParameters) {
			if (!(context instanceof JsonParameters)) {
				throw new IllegalArgumentException("DiscreteExternalTransition.setContextForClient: Client is TransitionJsonParameters but context is: " + context.getClass().getName());
			} else {
				((JsonParameters) context).setActiveTransition(getId());
			}
		}
		this.context = context; 
	}
	@Override
	public boolean equals(Object o) {
		if (!super.equals(o)) {
			return false;
		}
	    if (!(o instanceof DiscreteExternalTransition)) {
	        return false;
	    }
	    DiscreteExternalTransition that = (DiscreteExternalTransition) o;
		
	    if (className != that.className) {
	        return false;
	    }
	    return true;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public PlaceMarker getPlaceMarker() {
		return placeMarker;
	}

	@Override
	public ExecutablePetriNet getExecutablePetriNet() {
		return executablePetriNet;
	}

	@Override
	public Object getContext() {
		return context;
	}
}
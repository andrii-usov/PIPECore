package pipe.visitor;

import pipe.models.component.Connectable;
import pipe.models.component.PetriNetComponent;
import pipe.models.component.arc.Arc;
import pipe.models.component.arc.ArcVisitor;
import pipe.models.component.place.Place;
import pipe.models.component.place.PlaceVisitor;
import pipe.models.component.transition.Transition;
import pipe.models.component.transition.TransitionVisitor;
import pipe.models.petrinet.PetriNet;

import java.util.*;

public class PasteVisitor implements TransitionVisitor, ArcVisitor, PlaceVisitor {


    private final PetriNet petriNet;

    private final Collection<PetriNetComponent> components = new HashSet<PetriNetComponent>();

    private final Map<String, Connectable> createdConnectables = new HashMap<String, Connectable>();

    public Collection<PetriNetComponent> getCreatedComponents() {
        return createdComponents;
    }

    private final Collection<PetriNetComponent> createdComponents = new LinkedList<PetriNetComponent>();

    private final double xOffset;

    private final double yOffset;

    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components) {
        this.petriNet = petriNet;
        this.components.addAll(components);
        xOffset = 0;
        yOffset = 0;
    }

    public PasteVisitor(PetriNet petriNet, Collection<PetriNetComponent> components, double xOffset, double yOffset) {
        this.petriNet = petriNet;
        this.components.addAll(components);
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    @Override
    public <T extends Connectable, S extends Connectable> void visit(Arc<S, T> arc) {
        S source = arc.getSource();
        T target = arc.getTarget();

        if (components.contains(source)) {
            source = (S) createdConnectables.get(source.getId() + "_copied");
        }

        if (components.contains(target)) {
            target = (T) createdConnectables.get(target.getId() + "_copied");
        }

        Arc<S, T> newArc = new Arc<S, T>(source, target, arc.getTokenWeights(), arc.getType());
        setId(newArc);
        petriNet.addArc(newArc);
        createdComponents.add(newArc);

    }


    @Override
    public void visit(Place place) {
        Place newPlace = new Place(place);
        setId(newPlace);
        setName(newPlace);
        setOffset(newPlace);
        petriNet.addPlace(newPlace);
        createdConnectables.put(newPlace.getId(), newPlace);
        createdComponents.add(newPlace);
    }

    @Override
    public void visit(Transition transition) {
        Transition newTransition = new Transition(transition);
        setId(newTransition);
        setName(newTransition);
        setOffset(newTransition);
        petriNet.addTransition(newTransition);
        createdConnectables.put(newTransition.getId(), newTransition);
        createdComponents.add(newTransition);
    }


    private void setOffset(Connectable connectable) {
        connectable.setX(connectable.getX() + xOffset);
        connectable.setY(connectable.getY() + yOffset);
    }

    private void setId(PetriNetComponent component) {
        String id = component.getId() + "_copied";
        component.setId(id);
    }

    private void setName(Connectable component) {
        String name = component.getName() + "_copied";
        component.setName(name);
    }

}

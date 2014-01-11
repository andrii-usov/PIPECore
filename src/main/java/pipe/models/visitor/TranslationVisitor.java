package pipe.models.visitor;

import pipe.models.component.*;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

/**
 * Translates PetriNetComponents by a given amount
 */
public class TranslationVisitor implements PetriNetComponentVisitor {
    private final Point2D translation;
    private final Collection<PetriNetComponent> selected;

    public TranslationVisitor(Point2D translation, final Collection<PetriNetComponent> selected) {
        this.translation = translation;
        this.selected = selected;
    }

    @Override
    public void visit(final Arc<? extends Connectable, ? extends Connectable> arc) {
        if (selected.contains(arc.getSource()) && selected.contains(arc.getTarget())) {
            List<ArcPoint> points = arc.getIntermediatePoints();
            for (ArcPoint arcPoint : points) {
                Point2D point = arcPoint.getPoint();
                Point2D newPoint =
                        new Point2D.Double(point.getX() + translation.getX(), point.getY() + translation.getY());
                arcPoint.setPoint(newPoint);
            }
        }
    }

    @Override
    public void visit(final Place place) {
        place.setX(place.getX() + translation.getX());
        place.setY(place.getY() + translation.getY());

    }

    @Override
    public void visit(final Transition transition) {
        transition.setX(transition.getX() + translation.getX());
        transition.setY(transition.getY() + translation.getY());

    }

    @Override
    public void visit(final Token token) {

    }

    //    @Override
    //    public void visit(final RateParameter parameter) {
    //
    //    }
    //
    //    @Override
    //    public void visit(final StateGroup group) {
    //
    //    }

    @Override
    public void visit(final Annotation annotation) {

    }
}

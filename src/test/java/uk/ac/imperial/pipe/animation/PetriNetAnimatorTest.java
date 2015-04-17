package uk.ac.imperial.pipe.animation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;

import uk.ac.imperial.pipe.dsl.ANormalArc;
import uk.ac.imperial.pipe.dsl.APetriNet;
import uk.ac.imperial.pipe.dsl.APlace;
import uk.ac.imperial.pipe.dsl.AToken;
import uk.ac.imperial.pipe.dsl.AnImmediateTransition;
import uk.ac.imperial.pipe.dsl.AnInhibitorArc;
import uk.ac.imperial.pipe.exceptions.PetriNetComponentNotFoundException;
import uk.ac.imperial.pipe.models.petrinet.ExecutablePetriNet;
import uk.ac.imperial.pipe.models.petrinet.PetriNet;
import uk.ac.imperial.pipe.models.petrinet.Place;
import uk.ac.imperial.pipe.models.petrinet.Token;
import uk.ac.imperial.pipe.models.petrinet.Transition;
import uk.ac.imperial.pipe.visitor.ClonePetriNet;

public class PetriNetAnimatorTest {



    @Test
    public void correctlyIncrementsTokenCountInSelfLoop() throws PetriNetComponentNotFoundException {

        PetriNet petriNet = createSelfLoopPetriNet("1");
        Place place = petriNet.getComponent("P0", Place.class);
        place.setTokenCount("Default", 1);
        place.setCapacity(1);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        Place epnPlace = epn.getComponent("P0", Place.class);
        assertEquals(1, epnPlace.getTokenCount("Default"));
    }


    @Test
    public void firingFunctionalTransitionMovesTokens() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Red").withColor(Color.RED)).and(
                AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P0").containing(5, "Default").tokens()).and(APlace.withId("P1")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P0").andTarget("T1").with("#(P0)", "Default").tokens()).andFinally(
                ANormalArc.withSource("T1").andTarget("P1").with("#(P0)*2", "Red").tokens());
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Place ep1 = epn.getComponent("P0", Place.class);
        Place ep2 = epn.getComponent("P1", Place.class);
        assertEquals(0, ep1.getTokenCount("Default"));
        assertEquals(10, ep2.getTokenCount("Red"));
    }

    @Test
    public void firingTransitionDoesNotDisableTransition() throws PetriNetComponentNotFoundException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        Place place = petriNet.getComponent("P1", Place.class);
        Token token = petriNet.getComponent("Default", Token.class);
        place.setTokenCount(token.getId(), 2);

        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);


        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void firingTransitionEnablesNextTransition() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2")).and(
                AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2")).and(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).and(
                ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token()).andFinally(
                ANormalArc.withSource("P2").andTarget("T2").with("1", "Default").token());

        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Transition transition2 = epn.getComponent("T2", Transition.class);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition2);
    }
    @Test
    public void randomTransitionsReturnsSingleEligibleTransition() throws PetriNetComponentNotFoundException {
    	// PN with single enabled transition
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P1").containing(1, "Default").token()).
    			and(APlace.withId("P2")).
    			and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2")).and(
    			ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).andFinally(
    			ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token());
    	
    	ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
    	Animator animator = new PetriNetAnimator(epn);
    	animator.setRandom(new Random(123456l)); 
    	Transition t = animator.getRandomEnabledTransition(); 
    	assertEquals("T1", t.getId()); 
    }
    @Test
    public void randomTransitionsIncludesAllEligibleTransitionsInRoughProportion() throws PetriNetComponentNotFoundException {
    	// PN with two enabled transitions
    	PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
    			APlace.withId("P1").containing(1, "Default").token()).
    			and(APlace.withId("P2")).and(APlace.withId("P3")).
    			and(AnImmediateTransition.withId("T1")).and(AnImmediateTransition.withId("T2")).and(
    			ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token()).and(
    			ANormalArc.withSource("T1").andTarget("P2").with("1", "Default").token()).and(
    			ANormalArc.withSource("P1").andTarget("T2").with("1", "Default").token()).andFinally(
    			ANormalArc.withSource("T2").andTarget("P3").with("1", "Default").token());
    	
    	ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
    	Animator animator = new PetriNetAnimator(epn);
    	animator.setRandom(new Random(123456l)); 
    	assertEquals("T1", animator.getRandomEnabledTransition().getId()); 
    	assertEquals("T2", animator.getRandomEnabledTransition().getId()); 
    	assertEquals("T2", animator.getRandomEnabledTransition().getId()); 
    	assertEquals("roughly random","T1", animator.getRandomEnabledTransition().getId()); 
    }

    @Test
    public void firingTransitionBackwardMovesTokensBack() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(0, "Default").token()).and(AnImmediateTransition.withId("T1")).andFinally(
                ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransitionBackwards(transition);
        Place place = epn.getComponent("P1", Place.class);
        Token token = epn.getComponent("Default", Token.class);

        assertThat(place.getTokenCount(token.getId())).isEqualTo(1);
    }


    private PetriNet createSelfLoopPetriNet(String tokenWeight) {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P0")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("T1").andTarget("P0").with(tokenWeight, "Default").tokens()).andFinally(
                ANormalArc.withSource("P0").andTarget("T1").with(tokenWeight, "Default").tokens());
    }

    @Test
    public void correctlyEnablesTransitionEvenAfterFiring() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = createSimpleInhibitorPetriNet(1);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);
        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }



    @Test
    public void firingTransitionMovesToken() throws PetriNetComponentNotFoundException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Token token = epn.getComponent("Default", Token.class);
        Place p1 = epn.getComponent("P1", Place.class);
        Place p2 = epn.getComponent("P2", Place.class);
        assertEquals(0, p1.getTokenCount(token.getId()));
        assertEquals(1, p2.getTokenCount(token.getId()));
    }

    /**
     * Create simple Petri net with P1 -o T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return
     */
    public PetriNet createSimpleInhibitorPetriNet(int tokenWeight) {
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                APlace.withId("P2")).and(AnImmediateTransition.withId("T1")).and(
                AnInhibitorArc.withSource("P1").andTarget("T1")).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with(Integer.toString(tokenWeight), "Default").tokens());
    }

    @Test
    public void firingTransitionDisablesTransition() throws PetriNetComponentNotFoundException {
        int tokenWeight = 1;
        PetriNet petriNet = createSimplePetriNet(tokenWeight);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransition(transition);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).doesNotContain(transition);
    }
    @Test
    public void firingTransitionBackwardEnablesTransition() throws PetriNetComponentNotFoundException {
        PetriNet petriNet =
                APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(APlace.withId("P1")).and(
                        APlace.withId("P2").containing(1, "Default").token()).and(AnImmediateTransition.withId("T1")).andFinally(
                        ANormalArc.withSource("P1").andTarget("T1").with("1", "Default").token());
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        Animator animator = new PetriNetAnimator(epn);
        Transition transition = epn.getComponent("T1", Transition.class);
        animator.fireTransitionBackwards(transition);

        Collection<Transition> enabled = animator.getEnabledTransitions();
        assertThat(enabled).contains(transition);
    }

    @Test
    public void restoresPetriNet() throws PetriNetComponentNotFoundException {
        PetriNet petriNet = createSimplePetriNet(1);
        PetriNet copy = ClonePetriNet.clone(petriNet);
        ExecutablePetriNet epn = petriNet.getExecutablePetriNet(); 
        ExecutablePetriNet copyepn = copy.getExecutablePetriNet();
        Animator animator = new PetriNetAnimator(epn);
        animator.fireTransition(animator.getRandomEnabledTransition());
        
        animator.reset(); 
        assertEquals(copyepn, epn);
    }


    /**
     * Create simple Petri net with P1 -> T1 -> P2
     * Initialises a token in P1 and gives arcs A1 and A2 a weight of tokenWeight to a default token
     *
     * @param tokenWeight
     * @return
     */
    public PetriNet createSimplePetriNet(int tokenWeight) {
        String arcWeight = Integer.toString(tokenWeight);
        return APetriNet.with(AToken.called("Default").withColor(Color.BLACK)).and(
                APlace.withId("P1").containing(1, "Default").token()).and(APlace.withId("P2")).and(
                AnImmediateTransition.withId("T1")).and(
                ANormalArc.withSource("P1").andTarget("T1").with(arcWeight, "Default").tokens()).andFinally(
                ANormalArc.withSource("T1").andTarget("P2").with(arcWeight, "Default").tokens());
    }

}
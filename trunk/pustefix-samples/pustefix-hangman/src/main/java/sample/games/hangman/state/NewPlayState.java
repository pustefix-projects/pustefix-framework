package sample.games.hangman.state;

import org.springframework.beans.factory.annotation.Autowired;

import sample.games.hangman.context.ContextPlay;
import de.schlund.pfixcore.workflow.Context;
import de.schlund.pfixcore.workflow.app.DefaultIWrapperState;
import de.schlund.pfixxml.PfixServletRequest;
import de.schlund.pfixxml.ResultDocument;

public class NewPlayState extends DefaultIWrapperState {

    private ContextPlay contextPlay;
    
   @Override
   public ResultDocument getDocument(Context context, PfixServletRequest preq) throws Exception {
       contextPlay.reset();
       context.setJumpToPage("Play");
       return new ResultDocument();
   }
    
   @Autowired
   public void setContextPlay(ContextPlay contextPlay) {
       this.contextPlay = contextPlay;
   }
   
}

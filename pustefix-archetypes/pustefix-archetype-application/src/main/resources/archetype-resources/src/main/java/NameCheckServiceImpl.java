#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

public class NameCheckServiceImpl implements NameCheckService {

    @Override
    public boolean isValid(String name) {
        return name.length() > 2;
    }

}

package sistema;

import problema.*;
import ambiente.*;
import arvore.TreeNode;
import arvore.fnComparator;
import comuns.*;
import static comuns.PontosCardeais.*;
import java.util.ArrayList;

/**
 *
 * @author tacla
 */
public class Agente implements PontosCardeais
{
    /* referência ao ambiente para poder atuar no mesmo*/
    Model model;
    Estado estAtu; // guarda o estado atual (posição atual do agente)
    int plan[];
    double custo;
    static int ct = -1;
    static int andar = 0;
    static int vetor_caminho [] = {N, N, N, N, N, N, NE, L, L, L, L, L, SE, L};

    public Agente(Model m)
    {
        this.model = m;

        // Posiciona o agente fisicamente no ambiente na posicao inicial
        model.setPos(8,0);
    }

     /**
     * Agente escolhe qual acao será executada em um ciclo de raciocinio.
     * Observar que o agente executa somente uma acao por ciclo.
     */
    public int deliberar()
    {
        //  contador de acoes
        ct++;
        // @todo T1: perceber por meio do sensor a posicao atual e imprimir
        this.estAtu = sensorPosicao();
        System.out.println("Linha: " + estAtu.getLin() + " Coluna: " + estAtu.getCol());

        if(andar == 14)
        {
           return -1;
        }
        else
        {
            executarIr(vetor_caminho[andar]);
            andar++;
            return 1; // Se retornar -1, encerra o agente
        }

    }

    /**
    * Atuador: executa 'fisicamente' a acao Ir
    * @param direcao um dos pontos cardeais
    */
    public int executarIr(int direcao)
    {
        model.ir(direcao);
        //@todo T1 - invocar metodo do Model - atuar no ambiente
        return 1; // deu certo
    }

    /**
     * Simula um sensor que realiza a leitura da posição atual no ambiente e
     * traduz para um par de coordenadas armazenadas em uma instância da classe
     * Estado.
     * @return Estado contendo a posição atual do agente no labirinto
     */
    public Estado sensorPosicao()
    {
        //@todo T1 - sensor deve ler a posicao do agente no labirinto (environment)
        int pos_atual[] = model.lerPos();
        return new Estado(pos_atual[0],pos_atual[1]);
    }

}

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
    Estado proxEst;
    Problema instancia_problema;       // Instância do Problema que o Agente carrega consigo.
    int plan[];
    float custo;            // contador de custo já consumido
    static int ct = -1;     // contador de ações já tomadas.
    static int acao = 0;    // iterador do vetor plan

    public Agente(Model m)
    {
        this.model = m;

        // Fixa na cabeça do agente uma instância do problema.
        this.instancia_problema = new Problema();

        // Cria na cabeça do agente uma crença do labirinto atual.
        this.instancia_problema.criarLabirinto(9,9);

        this.instancia_problema.crencaLabir.porParedeVertical(0, 1, 0);
        this.instancia_problema.crencaLabir.porParedeVertical(0, 0, 1);
        this.instancia_problema.crencaLabir.porParedeVertical(5, 8, 1);
        this.instancia_problema.crencaLabir.porParedeVertical(5, 5, 2);
        this.instancia_problema.crencaLabir.porParedeVertical(8, 8, 2);
        this.instancia_problema.crencaLabir.porParedeHorizontal(4, 7, 0);
        this.instancia_problema.crencaLabir.porParedeHorizontal(7, 7, 1);
        this.instancia_problema.crencaLabir.porParedeHorizontal(3, 5, 2);
        this.instancia_problema.crencaLabir.porParedeHorizontal(3, 5, 3);
        this.instancia_problema.crencaLabir.porParedeHorizontal(7, 7, 3);
        this.instancia_problema.crencaLabir.porParedeVertical(6, 7, 4);
        this.instancia_problema.crencaLabir.porParedeVertical(5, 6, 5);
        this.instancia_problema.crencaLabir.porParedeVertical(5, 7, 7);
        
        // Seta Estado Inicial e Estado Objetivo na cabeça do agente.
        this.instancia_problema.defEstIni(8, 0);
        this.instancia_problema.defEstObj(2, 8);

        procura_caminho();
        // Mantém?
        //model.setPos(8,0);
    }

     /**
     * Agente escolhe qual acao será executada em um ciclo de raciocinio.
     * Observar que o agente executa somente uma acao por ciclo.
     */
    public int deliberar()
    {
        int acoesPossiveis[] = new int[8]; // Vetor de 8 posições.
        ct++;   //  contador de acoes
        
        this.estAtu = sensorPosicao();

        // Checa se já não alcançou o objetivo.
        if(this.estAtu.getLin() == this.instancia_problema.estObj.getLin())
        {
            if(this.estAtu.getCol() == this.instancia_problema.estObj.getCol())
            {
                // Imprime o custo final
                System.out.println("Custo acumulado: " + this.custo);
                // Encerra o agente.
                return -1;
            }
        } 

        // Se ainda não alcançou o objetivo, executa a ação do plano de ações atual.
        executarIr(this.plan[ct]);

        // Incrementa o custo atual
        if(this.plan[ct] == 0 || this.plan[ct] == 2 || this.plan[ct] == 4 || this.plan[ct] == 6)
            this.custo += 1;    // Selecionou ação N, S, L, O
        else
            this.custo += 1.5;  // Selecionou ação NE, SE, SO, NO

        // Imprime informações na tela

        System.out.println("Estado Atual: (" + estAtu.getLin() + ", " + estAtu.getCol() + ")");
        System.out.print("Ações possíveis: { ");
        acoesPossiveis = this.instancia_problema.acoesPossiveis(this.estAtu);
        for(int i = 0; i < 8; i++)
        {
            if(acoesPossiveis[i]>=0)
                System.out.printf("%s ", comuns.PontosCardeais.acao[i]);
            // Imprime as ações possíveis que o agente pode alcançar dado o estado atual.
        }
        System.out.println("}");
        System.out.println("Ações (ct): " + ct);
        System.out.println("Ação Escolhida: " + comuns.PontosCardeais.acao[this.plan[ct]]); 
        System.out.println("Custo até o momento: " + this.custo);

        return 1;

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

    public void procura_caminho()
    {
        // parte 1, tenta achar um caminho

        // Descobre onde o Agente está agora.
        Estado auxiliar = this.sensorPosicao();
        int linha = auxiliar.getLin();
        int coluna = auxiliar.getCol();
        
        // Limite de execução da busca
        boolean fim = false;
        int estados_ct = 0;

        // Salvar opções de rotas
        int vetor [] = new int[8];
        int matriz[][] = new int[9][9];

        // Preenche matriz com -1. Qualquer valor > -1 são opções alcançáveis
        for(int i = 0; i < 9; i++)
            for(int j = 0; j < 9; j++)
                matriz[i][j] = -1;

        matriz[linha][coluna] = estados_ct;
        // Enquanto não alcançar o objetivo ou verificar as 81 possibilidades de estados diferentes.
        while(fim == false && estados_ct < 81)
        {
            for(int i = 0; i < 9 && fim == false; i++) {
                for(int j = 0; j < 9 && fim == false; j++) {
                    
                    if(matriz[i][j] == estados_ct){
                        auxiliar.setLinCol(i,j);
                        // Verifica se alcançou o objetivo.
                        if(i == 2 && j == 8)
                            fim = true;
                        else{
                            // Se não está no objetivo, coloca no vetor as opções de ações que pode realizar
                            vetor = this.instancia_problema.acoesPossiveis(auxiliar);

                            // Olha para cada posição, e atualiza a matriz de opções.
                            if(vetor[0] == 0 && matriz[i-1][j] == -1) // Caso N.
                                matriz[i-1][j] = estados_ct+1;
                            
                            if(vetor[1] == 0 && matriz[i-1][j+1] == -1) // Caso NE.
                                matriz[i-1][j+1] = estados_ct+1;
            
                            if(vetor[2] == 0 && matriz[i][j+1] == -1) // Caso L.
                                matriz[i][j+1] = estados_ct+1;
                            
                            if(vetor[3] == 0 && matriz[i+1][j+1] == -1) // Caso SE.
                                matriz[i+1][j+1] = estados_ct+1;
                        
                            if(vetor[4] == 0 && matriz[i+1][j] == -1) // Caso S.
                                matriz[i+1][j] = estados_ct+1;                                
                        
                            if(vetor[5] == 0 && matriz[i+1][j-1] == -1) // Caso SO.
                                matriz[i+1][j-1] = estados_ct+1;                                
                        
                            if(vetor[6] == 0 && matriz[i][j-1] == -1) // Caso O.
                                matriz[i][j-1] = estados_ct+1;                                
                        
                            if(vetor[7] == 0 && matriz[i-1][j-1] == -1)  // Caso NO.
                                matriz[i-1][j-1] = estados_ct+1;                                
                        
                        }
                    }   
                }// for j 
            }// for i
            estados_ct++;
        }

        // parte 2 salva as informações no plano de ações.
        linha = 2;
        coluna = 8;
        estados_ct--;
        this.plan = new int[estados_ct];

        while(estados_ct > 0)
        {
    		if ((linha-1>=0) && matriz[linha-1][coluna]==estados_ct-1) {
    			linha--;
    			this.plan[estados_ct-1]=4;
    		}
    		else if ((coluna+1<9) && matriz[linha][coluna+1]==estados_ct-1) {
    			coluna++;
    			this.plan[estados_ct-1]=6;
    		}
    		else if ((linha+1<9) && matriz[linha+1][coluna]==estados_ct-1) {
    			linha++;
    			this.plan[estados_ct-1]=0;
    		}
    		else if ((coluna-1>=0) && matriz[linha][coluna-1]==estados_ct-1) {
    			coluna--;
    			this.plan[estados_ct-1]=2;
    		}
    		else if ((linha-1 >=0) && (coluna+1<9) && matriz[linha-1][coluna+1]==estados_ct-1) {
    			linha--;
    			coluna++;
    			this.plan[estados_ct-1]=5;
    		}
    		else if ((linha+1<9) && (coluna+1<9) && matriz[linha+1][coluna+1]==estados_ct-1) {
    			linha++;
    			coluna++;
    			this.plan[estados_ct-1]=7;
    		}
    		else if ((linha+1<9) && (coluna-1>=0) && matriz[linha+1][coluna-1]==estados_ct-1) {
    			linha++;
    			coluna--;
    			this.plan[estados_ct-1]=1;
    		}
    		else if ((linha-1>=0) && (coluna-1>=0) && matriz[linha-1][coluna-1]==estados_ct-1) {
    			linha--;
    			coluna--;
    			this.plan[estados_ct-1]=3;
    		}
    		estados_ct--;
		}
    }
    
}

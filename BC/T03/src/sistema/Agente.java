package sistema;

import problema.*;
import ambiente.*;
import arvore.TreeNode;
import arvore.fnComparator;
import comuns.*;
import static comuns.PontosCardeais.*;
import java.util.ArrayList;
import java.lang.*;
import java.util.*;
import java.util.List;

/**
 *
 * @author tacla
 */
public class Agente implements PontosCardeais
{
    /* referência ao ambiente para poder atuar no mesmo*/
    Model model;
    Estado estAtu; // guarda o estado atual (posição atual do agente)
    Estado estObj;
    Problema instancia_problema;       // Instância do Problema que o Agente carrega consigo.
    
    TreeNode arvore;
    TreeNode objetivo;
    List<TreeNode> fronteira = new ArrayList<>();
    List<TreeNode> sucessores = new ArrayList<>();
    List<Estado> visitados = new ArrayList<>();

    Estado caminho[] = null;

    static int ct = -1;     // contador de ações já tomadas.
    static int acao = 0;    // iterador do vetor plan
    double custo;
    int [] plan = new int [11];
    int i = 0;
    int escolha = 0;
    int [] slots_possiveis = new int [8];


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

        this.estAtu = this.instancia_problema.estIni;
        this.estObj = this.instancia_problema.estObj;

        // Escolhe um dos métodos para planejar
        this.CustoUniforme();
    }

     /**
     * Agente escolhe qual acao será executada em um ciclo de raciocinio.
     * Observar que o agente executa somente uma acao por ciclo.
     */
    public int deliberar()
    {

        /* Prints */
        System.out.println("Estado Atual: " + estAtu.getLin() + ", " + estAtu.getCol() );

        /* Deliberações */
        if(estAtu.igualAo(estObj) ==  false)
        {
            /* Não alcançou o objetivo ainda.*/
            this.ct++;
        }
        else
        {
            /* Alcançou objetivo, para de se mexer */
            return -1;
        }

        /* Escolhe a próxima ação */
        this.acao = this.plan[this.ct];

        /* Print */
        System.out.println("Ação Escolhida: " + PontosCardeais.acao[acao]);
        System.out.println("Custo Atual: " + this.custo);

        /* Executa ação */
        executarIr(acao);

        /* Atualiza o custo */
        att_custo(acao);

        /* Verifica onde está agora */
        estAtu = sensorPosicao();

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

    /* Calculo */

    public void att_custo(int acao)
    {
        // Se ações são: N, S, L, O, custo = 1;
        if(acao == 0 || acao == 2 || acao == 4 || acao == 6)
            this.custo++;
        else
            this.custo+=1.5;
        // Se ações são NE, NO, SE, SO, custo  = 1.5;
    }


    public float h1(Estado atual){
        return (float) Math.sqrt( (atual.getLin()-estObj.getLin())*(atual.getLin()-estObj.getLin()) + (atual.getCol()-estObj.getCol())*(atual.getCol()-estObj.getCol()) );
    }
    public float h2(Estado atual){
        return 0;
    }
    public void CustoUniforme(){
            boolean jaexiste = false;
            int no_lokao = -1;
            float min = 0 ;
            int act = 0 ;
            float custo = 0;
            float max_ct_ja_explorados = 0;
            float max_ct_descartados_front = 0;
            
    //Variaveis solicitadas pelo trabalho
            int ct_ja_explorados = 0;
            int ct_descartados_front = 0;
            int ct_substituidos_front = 0;
            int nos_gerados = 0;
            
            List<Estado> analisados = new ArrayList<>();  
            TreeNode atual = new TreeNode(null);
            atual.setState(estAtu);
            atual.setGn(0);
            arvore = atual;
            fronteira.add(atual);
            Estado analisado;
            Estado jaanalisado;
            analisados.add(atual.getState());
    
            // Enquanto a fronteira nao esta vazia
            while(fronteira.isEmpty()==false){
                
                if(ct_ja_explorados > max_ct_ja_explorados){
                    max_ct_ja_explorados = ct_ja_explorados;
                    ct_ja_explorados = 0;
                }
                else
                    ct_ja_explorados = 0;
    
                if(ct_descartados_front > max_ct_descartados_front){
                    max_ct_descartados_front = ct_descartados_front;
                    ct_descartados_front = 0;
                }
                else
                    ct_descartados_front = 0;       
                
                // Remove o atual da fronteira
                fronteira.remove(atual);
                // Se o estado atual eh o objetivo
                if (atual.getState().igualAo(estObj) == true)
                    objetivo = atual;
    
                // Itera nas acoes 
                for(act=0;act<8;act++){
                    // Coleta em analisado uma das sucessoras
                    analisado = instancia_problema.suc(atual.getState(), act);
    
                    // Se o analisado eh igual ao estado atual, significa que e uma parede
                    // Nao faz nada
                    if(analisado.equals(atual.getState()) == false){
                        // Se nao for igual, quer dizer que o estado e possivel
                        for (int i = 0; i < analisados.size(); i++){
                            // Itera em analisados 
                            // Tira de analisados e iguala a jaanalisado
                            jaanalisado = analisados.get(i) ;
                            // Se o jaanalisado == analisado
                            if (jaanalisado.igualAo(analisado) == true){
                                // Ja existe na arvore de analisados
                                ct_ja_explorados++;
                                jaexiste = true;
                                // Itera na fronteira 
                                for (int j = 0; j < fronteira.size(); j++)
                                    if (fronteira.get(j).getState().igualAo(analisado))
                                        // Vou ver x vezes o analisado 
                                        no_lokao = j;
                                 
                            }
                        }
                        // Se ele existe 
                        // nolokao guarda o index desse cara
                        if(jaexiste){
                            // Se for diferente de -1, quer dizer que ainda tem um cara na fronteira
                            if(no_lokao != -1){
                                // Adiciona mais um filho no cara
                                TreeNode novo = atual.addChild();
                                nos_gerados++;
                                // Seta acao e estado
                                novo.setAction(act);
                                novo.setState(analisado);
                                // Analisa custo
                                if(act % 2 == 0)
                                    novo.setGn((float)(atual.getGn()+1));
                                else
                                    novo.setGn((float)(atual.getGn()+1.5));
                                
                                // Se fronteira[no_lokao] ter custo maior que o novo
                                // Remove o no lokao e coloca o no novo na fronteira, pq custa mais
                                if (fronteira.get(no_lokao).getGn() > novo.getGn()){
                                    ct_substituidos_front++;
                                    fronteira.remove(fronteira.get(no_lokao));
                                    fronteira.add(novo); 
                                }
                                else
                                    ct_descartados_front++;
                                    
                                no_lokao=-1;
                            }
                            jaexiste = false;
                                
                        }
                        else{
                            TreeNode novo = atual.addChild();
                            nos_gerados++;
                            if(act % 2 == 0)
                                novo.setGn((float)(atual.getGn()+1));
                            else
                                novo.setGn((float)(atual.getGn()+1.5));
                            novo.setAction(act);
                            novo.setState(analisado);                		
                            fronteira.add(novo);
                            analisados.add(analisado);
                        }
                    }
    
                }
                if (fronteira.isEmpty()==false){
                    min = fronteira.get(0).getGn();
                    atual = fronteira.get(0);
                    for (int i = 1; i < fronteira.size(); i++){
                        if (fronteira.get(i).getGn()< min) {
                            atual = fronteira.get(i);
                        }
                    }
                }   
            }
            plan = new int[objetivo.getDepth()];
            atual = objetivo;
            for(int i=objetivo.getDepth()-1;i>=0;atual=atual.getParent()){
                plan[i--]=atual.getAction();
            }
            
            System.out.println("Plano de acoes:");
            
            for(int i=0;i<plan.length;i++)
                System.out.printf(PontosCardeais.acao[plan[i]]+" - ");
            
            System.out.println("\nCusto:"+objetivo.getGn());
    
            
        }
    public void AEstrelaH1(){
            boolean jaexiste = false;
            int no_lokao = -1;
            float min = 0 ;
            float max_ct_ja_explorados = 0;
            float max_ct_descartados_front = 0;
            int act = 0 ;
            float custo = 0;
            int nos_gerados = 0;
          
            //Variaveis solicitadas pelo trabalho
            int ct_ja_explorados = 0;
            int ct_descartados_front = 0;
            int ct_substituidos_front = 0;
            
            List<Estado> analisados = new ArrayList<>();  
            TreeNode atual = new TreeNode(null);
            nos_gerados++;
            atual.setState(estAtu);
            atual.setHn(h1(atual.getState()));
            atual.setGn(0);
            arvore = atual;
            fronteira.add(atual);
            Estado analisado;
            Estado jaanalisado;
            analisados.add(atual.getState());
    
            while(fronteira.isEmpty()==false){
                
                if(ct_ja_explorados > max_ct_ja_explorados){
                    max_ct_ja_explorados = ct_ja_explorados;
                    ct_ja_explorados = 0;
                }
                else
                    ct_ja_explorados = 0;
    
                if(ct_descartados_front > max_ct_descartados_front){
                    max_ct_descartados_front = ct_descartados_front;
                    ct_descartados_front = 0;
                }
                else
                    ct_descartados_front = 0;            
                
                fronteira.remove(atual);
                if (atual.getState().igualAo(estObj) == true){
                    objetivo = atual;
                    fronteira.removeAll(fronteira);
                }
                else {
                    for(act=0;act<8;act++){
                        analisado = instancia_problema.suc(atual.getState(), act);
    
                        if(analisado.equals(atual.getState()) == false){
                            for (int i = 0; i < analisados.size(); i++){
                                jaanalisado = analisados.get(i) ;
                                if (jaanalisado.igualAo(analisado) == true){
                                    ct_ja_explorados++;                            
                                    jaexiste = true;
                                    for (int j = 0; j < fronteira.size(); j++)
                                        if (fronteira.get(j).getState().igualAo(analisado))
                                            no_lokao = j;
    
                                }
                            }
                            if(jaexiste){
                                if(no_lokao != -1){
                                    TreeNode novo = atual.addChild();
                                    nos_gerados++;
                                    novo.setAction(act);
                                    novo.setState(analisado);
                                    novo.setHn(h1(novo.getState()));
                                    if(act % 2 == 0)
                                        novo.setGn((float)(atual.getGn()+1));
                                    else
                                        novo.setGn((float)(atual.getGn()+1.5));
    
                                    if (fronteira.get(no_lokao).getFn() > novo.getFn()){
                                        ct_substituidos_front++;                                
                                        fronteira.remove(fronteira.get(no_lokao));
                                        fronteira.add(novo); 
                                    }
                                    else
                                        ct_descartados_front++;
                                    
                                        
                                    no_lokao=-1;
                                }
                                jaexiste = false;
    
                            }
                            else{
                                TreeNode novo = atual.addChild();
                                nos_gerados++;
                                if(act % 2 == 0)
                                    novo.setGn((float)(atual.getGn()+1));
                                else
                                    novo.setGn((float)(atual.getGn()+1.5));
                                novo.setAction(act);
                                novo.setState(analisado);                		
                                novo.setHn(h1(novo.getState()));
                                fronteira.add(novo);
                                analisados.add(analisado);
                            }
                        }
    
                    }
                    if (fronteira.isEmpty()==false){
                        min = fronteira.get(0).getFn();
                        atual = fronteira.get(0);
                        for (int i = 1; i < fronteira.size(); i++){
                            if (fronteira.get(i).getFn()< min) {
                                atual = fronteira.get(i);
                            }
                        }
                    }
                }
                   
            }
            plan = new int[objetivo.getDepth()];
            atual = objetivo;
            
            for(int i=objetivo.getDepth()-1;i>=0;atual=atual.getParent()){
                plan[i--]=atual.getAction();
            }
    
            System.out.print("Plano de acoes:");
            
            for(int i=0;i<plan.length;i++)
                System.out.printf(PontosCardeais.acao[plan[i]]+" - ");
            
            System.out.println("\nCusto:"+objetivo.getGn());
       }
    public void AEstrelaH2(){
            boolean jaexiste = false;
            int no_lokao = -1;
            float min = 0 ;
            int act = 0 ;
            float custo = 0;
            float max_ct_ja_explorados = 0;
            float max_ct_descartados_front = 0;
            int nos_gerados = 0;
          
            //Variaveis solicitadas pelo trabalho
            int ct_ja_explorados = 0;
            int ct_descartados_front = 0;
            int ct_substituidos_front = 0;
            
            List<Estado> analisados = new ArrayList<>();  
            TreeNode atual = new TreeNode(null);
            nos_gerados++;
            atual.setState(estAtu);
            atual.setHn(h2(atual.getState()));
            atual.setGn(0);
            arvore = atual;
            fronteira.add(atual);
            Estado analisado;
            Estado jaanalisado;
            analisados.add(atual.getState());
    
            while(fronteira.isEmpty()==false){
                
                if(ct_ja_explorados > max_ct_ja_explorados){
                    max_ct_ja_explorados = ct_ja_explorados;
                    ct_ja_explorados = 0;
                }
                else
                    ct_ja_explorados = 0;
    
                if(ct_descartados_front > max_ct_descartados_front){
                    max_ct_descartados_front = ct_descartados_front;
                    ct_descartados_front = 0;
                }
                else
                    ct_descartados_front = 0;
                
                fronteira.remove(atual);
                if (atual.getState().igualAo(estObj) == true){
                    objetivo = atual;
                    fronteira.removeAll(fronteira);
                }
                else {
                    for(act=0;act<8;act++){
                        analisado = instancia_problema.suc(atual.getState(), act);
    
                        if(analisado.equals(atual.getState()) == false){
                            for (int i = 0; i < analisados.size(); i++){
                                jaanalisado = analisados.get(i) ;
                                if (jaanalisado.igualAo(analisado) == true){
                                    ct_ja_explorados++;                            
                                    jaexiste = true;
                                    for (int j = 0; j < fronteira.size(); j++)
                                        if (fronteira.get(j).getState().igualAo(analisado))
                                            no_lokao = j;
    
                                }
                            }
                            if(jaexiste){
                                if(no_lokao != -1){
                                    TreeNode novo = atual.addChild();
                                    nos_gerados++;
                                    novo.setAction(act);
                                    novo.setState(analisado);
                                    novo.setHn(h2(novo.getState()));
                                    if(act % 2 == 0)
                                        novo.setGn((float)(atual.getGn()+1));
                                    else
                                        novo.setGn((float)(atual.getGn()+1.5));
    
                                    if (fronteira.get(no_lokao).getFn() > novo.getFn()){
                                        ct_substituidos_front++;                                
                                        fronteira.remove(fronteira.get(no_lokao));
                                        fronteira.add(novo); 
                                    }
                                    else
                                        ct_descartados_front++;
                                    
                                        
                                    no_lokao=-1;
                                }
                                jaexiste = false;
    
                            }
                            else{
                                TreeNode novo = atual.addChild();
                                nos_gerados++;
                                if(act % 2 == 0)
                                    novo.setGn((float)(atual.getGn()+1));
                                else
                                    novo.setGn((float)(atual.getGn()+1.5));
                                novo.setAction(act);
                                novo.setState(analisado);                		
                                novo.setHn(h2(novo.getState()));
                                fronteira.add(novo);
                                analisados.add(analisado);
                            }
                        }
    
                    }
                    if (fronteira.isEmpty()==false){
                        min = fronteira.get(0).getFn();
                        atual = fronteira.get(0);
                        for (int i = 1; i < fronteira.size(); i++){
                            if (fronteira.get(i).getFn()< min) {
                                atual = fronteira.get(i);
                            }
                        }
                    }
                }
                   
            }
            plan = new int[objetivo.getDepth()];
            atual = objetivo;
            for(int i=objetivo.getDepth()-1;i>=0;atual=atual.getParent()){
                plan[i--]=atual.getAction();
            }
            
            System.out.println("Plano de acoes:");
            
            for(int i=0;i<plan.length;i++)
                System.out.printf(PontosCardeais.acao[plan[i]]+" - ");
            
            System.out.println("\nCusto:"+objetivo.getGn());
    
       }  
}

/* Extra functions */

/*
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
    */
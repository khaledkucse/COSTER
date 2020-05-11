import random
random.seed()

values = ((0.65,0.26,0,52),
          (0.77,0.19,0.55),
          (0.73,0.28,0.48),
          (0.62,0.21,0.58),
          (0.86,0.33,0.59))

# Defining our mathematical function
def func (alpha=None, beta=None, gamma=0):
    '''
    Params: the coeffient values in a tupple.
    Return: the value for each combination of the parameters
    '''
    try:
        picked_value = values[random.randint(0, len(values) - 1)]
        return alpha* picked_value[0] + beta*picked_value[1] + gamma*picked_value[2]
    except:
        print('Mistake at func() method')
        return None

def radial_dir(pos_init, p=0.1, ob='min'):
    """
    Create 16 neighborhoods around pos_init
    Receives: (1) starting position = tuple with x and y
              (2) radial step size = float
              (3) goal being 'max' or 'min'
    Return: tuple with a tuple of the pairs of the lowest position in relation to pos_init and a flag
            'is_optimal' which indicates if it has reached the minimum possible or not yet
    """

    is_optimal = False

    # Assign coordinates to variables alpha, beta and gamma
    alpha = pos_init[0]
    beta = pos_init[1]
    gamma = pos_init[2]

    # Create a dictionary to map the deltas for each surrounding coordinate.
    # Strategy is to put deltas as keys and coordinates as values.
    # This will simplify the analysis later

    dicti = {}

    try:

        # Single Change
        dicti[func(alpha, beta, gamma) - func(alpha + p, beta, gamma)] = (alpha + p, beta, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha, beta + p, gamma)] = (alpha, beta + p, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha, beta, gamma + p)] = (alpha, beta, gamma + p)

        dicti[func(alpha, beta, gamma) - func(alpha - p, beta, gamma)] = (alpha - p, beta, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha, beta - p, gamma)] = (alpha, beta - p, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha, beta, gamma-p)] = (alpha, beta, gamma - p)

        #Dual Change
        dicti[func(alpha, beta, gamma) - func(alpha + p, beta + p, gamma)] = (alpha + p, beta +p, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha + p, beta, gamma + p)] = (alpha + p, beta, gamma + p)

        dicti[func(alpha, beta, gamma) - func(alpha, beta + p, gamma + p)] = (alpha, beta + p, gamma + p)

        dicti[func(alpha, beta, gamma) - func(alpha - p, beta - p, gamma)] = (alpha - p, beta - p, gamma)

        dicti[func(alpha, beta, gamma) - func(alpha - p, beta, gamma - p)] = (alpha - p, beta, gamma - p)

        dicti[func(alpha, beta, gamma) - func(alpha, beta - p, gamma - p)] = (alpha, beta - p, gamma - p)


        # Equational Changes
        dicti[func(alpha, beta, gamma) - func(alpha + p / (2 ** (0.5)), beta + p / (2 ** (0.5)), gamma + p / (2 ** (0.5)))] = (
        alpha + p / (2 ** (0.5)), beta + p / (2 ** (0.5)), gamma + p / (2 ** (0.5)))

        dicti[func(alpha, beta, gamma) - func(alpha + p / (2 ** (0.5)), beta - p / (2 ** (0.5)), gamma - p / (2 ** (0.5)))] = (
        alpha + p / (2 ** (0.5)), beta - p / (2 ** (0.5)), gamma - p / (2 ** (0.5)))

        dicti[func(alpha, beta, gamma) - func(alpha - p / (2 ** (0.5)), beta - p / (2 ** (0.5)),gamma - p / (2 ** (0.5)))] = (
        alpha - p / (2 ** (0.5)), beta - p / (2 ** (0.5)), gamma - p / (2 ** (0.5)))

        dicti[func(alpha, beta, gamma) - func(alpha - p / (2 ** (0.5)), beta + p / (2 ** (0.5)), gamma + p / (2 ** (0.5)))] = (
        alpha - p / (2 ** (0.5)), beta + p / (2 ** (0.5)), gamma + p / (2 ** (0.5)))

    except:
        print('There was a flaw in the construction of the dictionary.')

    try:
        # Let's look at minimization first. In this case, we will look for a point whose delta
        # between (current position) - (calculated position) is as large as possible.
        # If there is no positive delta, in this case, the current point is the optimal point.
        if (ob == 'min'):
            if (max(dicti.keys()) <= 0):
                print('Current minimum point: ', (alpha, beta, gamma))
                print('Value: ', func(alpha, beta, gamma))
                is_optimal = True
                return ((alpha, beta, gamma), is_optimal)
            else:
                return (dicti[max(dicti.keys())], is_optimal)

        # We will now address maximization. In this case, we will look for a point whose delta
        # between (current position) - (calculated position) is as small as possible.
        # If there is no negative delta, in this case, the current point is the optimal point.

        elif (ob == 'max'):
            if (min(dicti.keys()) >= 0):
                print('Current maximum point: ', (alpha, beta, gamma))
                is_optimal = True
                return ((alpha, beta, gamma), is_optimal)
            else:
                print('Current maximum point: ', dicti[min(dicti.keys())])
                return (dicti[min(dicti.keys())], is_optimal)

    except:
        print('There was some failure to return to position.')


# Initial Paramerters
step_size = 0.1
pos_init = (0, 0, 0)
ob = 'max'
rounds = list(range(1000))

# run the program consecutive times pursuing the optimization goal
for r in rounds:
    pos_init, flag = radial_dir(pos_init, p=step_size, ob=ob)
    if (flag):
        print('The algorithm reaches at the optimal points!')
        break

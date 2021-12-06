import sys

import matplotlib.pyplot as plt

plt.rcParams['figure.figsize'] = [15, 10]


def plot_nodes(input_file):
    x_coords = []
    y_coords = []
    capacity = []
    with open(input_file, "r") as f:
        lines = f.readlines()
        load_coordinates = False
        load_capacity = False
        for line in lines:
            line = line.strip()
            #             print(line)
            if line == 'NODE_COORD_SECTION':
                load_coordinates = True
                continue
            elif line == 'DEMAND_SECTION':
                load_coordinates = False
                load_capacity = True
                continue
            elif line == 'DEPOT_SECTION':
                load_capacity = False
                break

            if load_coordinates:
                i, x, y = [float(each) for each in line.split()]
                # print(line, x, y)
                x_coords.append(x)
                y_coords.append(y)
            if load_capacity:
                i, c = [float(each) for each in line.split()]
                capacity.append(c)

    # print(list(zip(x_coords, y_coords)))
    plt.scatter(x_coords, y_coords)
    for i in range(len(capacity)):
        plt.annotate(i + 1, (x_coords[i], y_coords[i]), (x_coords[i], y_coords[i] - 3))
    return x_coords, y_coords


def plot_routes(output_file, x_coords, y_coords):
    with open(output_file, 'r') as f:
        lines = f.readlines()
        for line in lines:
            line = line.strip()
            if not line.startswith('Route'):
                continue
            _, _, *points = line.split()
            points = [int(each) for each in points]
            sol_x_coords = [x_coords[0]] + [x_coords[i] for i in points] + [x_coords[0]]
            sol_y_coords = [y_coords[0]] + [y_coords[i] for i in points] + [y_coords[0]]
            #             print(sol_x_coords)
            #             print(sol_y_coords)
            #             print('-'*10)
            plt.scatter(sol_x_coords, sol_y_coords)
            plt.plot(sol_x_coords, sol_y_coords)


def plot_route(output_file, x_coords, y_coords, route_num):
    with open(output_file, 'r') as f:
        lines = f.readlines()
        for line in lines:
            line = line.strip()
            if not line.startswith('Route #' + str(route_num) + ":"):
                continue
            _, _, *points = line.split()
            points = [int(each) for each in points]
            sol_x_coords = [x_coords[0]] + [x_coords[i] for i in points] + [x_coords[0]]
            sol_y_coords = [y_coords[0]] + [y_coords[i] for i in points] + [y_coords[0]]
            #             print(sol_x_coords)
            #             print(sol_y_coords)
            #             print('-'*10)
            plt.scatter(sol_x_coords, sol_y_coords)
            plt.plot(sol_x_coords, sol_y_coords)


def main(argv: list[str]):
    vrp_path = argv[1]
    sol_path = argv[2]
    route_num = None
    if len(argv) >= 4:
        route_num = argv[3]

    x_coords, y_coords = plot_nodes(vrp_path)

    if route_num is not None:
        plot_route(sol_path, x_coords, y_coords, route_num)
    else:
        plot_routes(sol_path, x_coords, y_coords)

    plt.show()


if __name__ == '__main__':
    main(sys.argv)
